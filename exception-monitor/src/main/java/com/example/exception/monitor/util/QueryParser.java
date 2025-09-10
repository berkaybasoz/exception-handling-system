package com.example.exception.monitor.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class QueryParser {
    
    private static final Pattern TERM_PATTERN = Pattern.compile(
        "(?i)(?:NOT\\s+)?" +  // Optional NOT operator
        "([\\w.]+):" +         // Field name (including dots for nested fields)
        "(?:\"([^\"]+)\"|" +   // Quoted value
        "([^\\s()]+))"         // Or unquoted value
    );
    
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(
        "\\s+(AND|OR)\\s+", Pattern.CASE_INSENSITIVE
    );
    
    @Data
    public static class QueryTerm {
        private String field;
        private String value;
        private boolean negated;
        private boolean wildcard;
        private String wildcardType; // "prefix", "suffix", "contains", "exists"
        
        public QueryTerm(String field, String value, boolean negated) {
            this.field = field;
            this.negated = negated;
            
            // Process wildcards
            if (value.equals("*")) {
                this.wildcardType = "exists";
                this.wildcard = true;
                this.value = value;
            } else if (value.startsWith("*") && value.endsWith("*")) {
                this.wildcardType = "contains";
                this.wildcard = true;
                this.value = value.substring(1, value.length() - 1);
            } else if (value.startsWith("*")) {
                this.wildcardType = "suffix";
                this.wildcard = true;
                this.value = value.substring(1);
            } else if (value.endsWith("*")) {
                this.wildcardType = "prefix";
                this.wildcard = true;
                this.value = value.substring(0, value.length() - 1);
            } else {
                this.wildcardType = null;
                this.wildcard = false;
                this.value = value;
            }
        }
    }
    
    @Data
    public static class ParsedQuery {
        private List<QueryTerm> terms = new ArrayList<>();
        private List<String> operators = new ArrayList<>(); // AND, OR between terms
        private Map<String, List<QueryTerm>> fieldGroups = new HashMap<>();
        
        public void addTerm(QueryTerm term) {
            terms.add(term);
            
            // Group by field prefix (headers, params, additionalData)
            String prefix = getFieldPrefix(term.getField());
            fieldGroups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(term);
        }
        
        private String getFieldPrefix(String field) {
            if (field.startsWith("headers.")) return "headers";
            if (field.startsWith("params.")) return "params";
            if (field.startsWith("additionalData.")) return "additionalData";
            return "standard";
        }
        
        public String buildSQLCondition() {
            if (terms.isEmpty()) {
                return null;
            }
            
            StringBuilder sql = new StringBuilder();
            
            for (int i = 0; i < terms.size(); i++) {
                QueryTerm term = terms.get(i);
                
                if (i > 0 && i - 1 < operators.size()) {
                    sql.append(" ").append(operators.get(i - 1)).append(" ");
                }
                
                sql.append(buildTermCondition(term));
            }
            
            return sql.toString();
        }
        
        private String buildTermCondition(QueryTerm term) {
            StringBuilder condition = new StringBuilder();
            
            if (term.isNegated()) {
                condition.append("NOT ");
            }
            
            condition.append("(");
            
            String field = term.getField();
            String value = term.getValue();
            
            // Handle different field types
            if (field.startsWith("headers.")) {
                String headerName = field.substring(8);
                if (headerName.equals("*")) {
                    // Search in all headers
                    condition.append("e.additionalData LIKE '%\"httpHeaders\":%")
                            .append(value).append("%'");
                } else {
                    // Search specific header
                    if (term.getWildcardType() != null) {
                        switch (term.getWildcardType()) {
                            case "exists":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(headerName).append("\":%'");
                                break;
                            case "contains":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(headerName).append("\":\"%")
                                        .append(value).append("%\"%'");
                                break;
                            case "prefix":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(headerName).append("\":\"")
                                        .append(value).append("%\"%'");
                                break;
                            case "suffix":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(headerName).append("\":\"%")
                                        .append(value).append("\"%'");
                                break;
                        }
                    } else {
                        condition.append("e.additionalData LIKE '%\"")
                                .append(headerName).append("\":\"")
                                .append(value).append("\"%'");
                    }
                }
            } else if (field.startsWith("params.")) {
                String paramName = field.substring(7);
                if (paramName.equals("*")) {
                    // Search in all params
                    condition.append("e.additionalData LIKE '%\"requestParameters\":%")
                            .append(value).append("%'");
                } else {
                    // Search specific param
                    if (term.getWildcardType() != null) {
                        switch (term.getWildcardType()) {
                            case "exists":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(paramName).append("\":%'");
                                break;
                            case "contains":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(paramName).append("\":\"%")
                                        .append(value).append("%\"%'");
                                break;
                            case "prefix":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(paramName).append("\":\"")
                                        .append(value).append("%\"%'");
                                break;
                            case "suffix":
                                condition.append("e.additionalData LIKE '%\"")
                                        .append(paramName).append("\":\"%")
                                        .append(value).append("\"%'");
                                break;
                        }
                    } else {
                        condition.append("e.additionalData LIKE '%\"")
                                .append(paramName).append("\":\"")
                                .append(value).append("\"%'");
                    }
                }
            } else if (field.startsWith("additionalData.")) {
                String dataField = field.substring(15);
                if (term.getWildcardType() != null) {
                    switch (term.getWildcardType()) {
                        case "exists":
                            condition.append("e.additionalData LIKE '%\"")
                                    .append(dataField).append("\":%'");
                            break;
                        case "contains":
                            condition.append("e.additionalData LIKE '%\"")
                                    .append(dataField).append("\":%")
                                    .append(value).append("%'");
                            break;
                        case "prefix":
                            condition.append("e.additionalData LIKE '%\"")
                                    .append(dataField).append("\":\"")
                                    .append(value).append("%'");
                            break;
                        case "suffix":
                            condition.append("e.additionalData LIKE '%\"")
                                    .append(dataField).append("\":%")
                                    .append(value).append("\"%'");
                            break;
                    }
                } else {
                    // Handle both quoted string values and unquoted values in JSON
                    condition.append("(e.additionalData LIKE '%\"")
                            .append(dataField).append("\":\"")
                            .append(value).append("\"%' OR e.additionalData LIKE '%\"")
                            .append(dataField).append("\":")
                            .append(value).append("%')");
                }
            } else {
                // Standard fields (exceptionType, message, environment, etc.)
                String dbField = mapToDbField(field);
                if (dbField != null) {
                    if (term.getWildcardType() != null) {
                        switch (term.getWildcardType()) {
                            case "exists":
                                condition.append("e.").append(dbField).append(" IS NOT NULL");
                                break;
                            case "contains":
                                condition.append("e.").append(dbField).append(" LIKE '%")
                                        .append(value).append("%'");
                                break;
                            case "prefix":
                                condition.append("e.").append(dbField).append(" LIKE '")
                                        .append(value).append("%'");
                                break;
                            case "suffix":
                                condition.append("e.").append(dbField).append(" LIKE '%")
                                        .append(value).append("'");
                                break;
                        }
                    } else {
                        condition.append("e.").append(dbField).append(" = '")
                                .append(value).append("'");
                    }
                }
            }
            
            condition.append(")");
            
            return condition.toString();
        }
        
        private String mapToDbField(String field) {
            Map<String, String> fieldMap = new HashMap<>();
            fieldMap.put("exceptionType", "exceptionType");
            fieldMap.put("message", "message");
            fieldMap.put("environment", "environment");
            fieldMap.put("projectName", "projectName");
            fieldMap.put("componentName", "componentName");
            fieldMap.put("serviceName", "serviceName");
            fieldMap.put("method", "method");
            fieldMap.put("podName", "podName");
            fieldMap.put("podIp", "podIp");
            
            return fieldMap.get(field);
        }
    }
    
    public ParsedQuery parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ParsedQuery();
        }
        
        ParsedQuery parsed = new ParsedQuery();
        
        // Remove parentheses for now (simplified parsing)
        query = query.replaceAll("[()]", "");
        
        // Find all operators
        Matcher operatorMatcher = OPERATOR_PATTERN.matcher(query);
        List<String> operators = new ArrayList<>();
        while (operatorMatcher.find()) {
            operators.add(operatorMatcher.group(1).toUpperCase());
        }
        parsed.setOperators(operators);
        
        // Split by operators and process each term
        String[] parts = query.split("\\s+(?:AND|OR)\\s+", -1);
        
        for (String part : parts) {
            part = part.trim();
            boolean negated = false;
            
            // Check for NOT operator
            if (part.toUpperCase().startsWith("NOT ")) {
                negated = true;
                part = part.substring(4).trim();
            }
            
            Matcher termMatcher = TERM_PATTERN.matcher(part);
            if (termMatcher.find()) {
                String field = termMatcher.group(1);
                String value = termMatcher.group(2) != null ? termMatcher.group(2) : termMatcher.group(3);
                
                QueryTerm term = new QueryTerm(field, value, negated);
                parsed.addTerm(term);
            }
        }
        
        return parsed;
    }
}