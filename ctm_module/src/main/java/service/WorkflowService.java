package service;

import client.model.Task;
import model.WorkflowRule;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class WorkflowService {
    private List<WorkflowRule> rules = new ArrayList<>();

    public WorkflowService() {
        // Sample rule: If deadline is today, set priority to High
        rules.add(new WorkflowRule("1", "DEADLINE_IS_TODAY", "", "SET_PRIORITY", "High"));
    }

    public void applyRules(Task task) {
        for (WorkflowRule rule : rules) {
            if (evaluateCondition(task, rule)) {
                executeAction(task, rule);
            }
        }
    }

    private boolean evaluateCondition(Task task, WorkflowRule rule) {
        switch (rule.getConditionType()) {
            case "DEADLINE_IS_TODAY":
                return task.getDeadline().equals(LocalDate.now().toString());
            case "PRIORITY_EQUALS":
                return task.getPriority().equals(rule.getConditionValue());
            default:
                return false;
        }
    }

    private void executeAction(Task task, WorkflowRule rule) {
        switch (rule.getActionType()) {
            case "SET_PRIORITY":
                task.setPriority(rule.getActionValue());
                break;
            case "SET_STATUS":
                task.setStatus(rule.getActionValue());
                break;
        }
    }
}
