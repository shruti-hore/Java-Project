package model;

public class WorkflowRule {
    private String id;
    private String conditionType; // e.g., "DEADLINE_PASSED", "PRIORITY_EQUALS"
    private String conditionValue;
    private String actionType; // e.g., "SET_PRIORITY", "SET_STATUS"
    private String actionValue;

    public WorkflowRule(String id, String conditionType, String conditionValue, String actionType, String actionValue) {
        this.id = id;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.actionType = actionType;
        this.actionValue = actionValue;
    }

    public String getConditionType() { return conditionType; }
    public String getConditionValue() { return conditionValue; }
    public String getActionType() { return actionType; }
    public String getActionValue() { return actionValue; }
}
