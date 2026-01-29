package DJT_Company;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductLine {
    private int lineId;
    private String lineName;
    private String status;
    private List<Task> Tasks;

    public ProductLine(int lineId, String lineName) {
        this.lineId = lineId;
        this.lineName = lineName;
        this.status = "Stopped";
        this.Tasks = new CopyOnWriteArrayList<>();
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public synchronized  String getStatus() {
        return status;
    }

    public synchronized void setStatus(String status) {
        try {
            if (status.equals("Active") || status.equals("Stopped")||status.equals("Maintenance")) {
                this.status = status;
            }
            else {
                throw new IllegalArgumentException("There is no such status");
            }
        } catch (IllegalArgumentException e) {
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
        }
    }

    public List<Task> getTasks(){
        return Tasks;
    }

    public void addTask(Task task){
        synchronized (this) {
            if(this.status.equals("Active")){
                Tasks.add(task);
                task.setAssignedLine(this);
                System.out.println("Task: " + task.getTaskId() + " added to product line: " + lineName);
            }
            else if (this.status.equals("Maintenance")) {
                System.out.println("Line addition failed due to Maintenance ");
            }
            else{
                System.out.println("Cannot add task to an inactive product line");
            }
        }
    }

    public void removeTask(Task task){
        if(Tasks.remove(task)){
            task.setAssignedLine(null);
            System.out.println("Done task: " + task.getTaskId() + " for product line: " + lineName);
        }
    }

    public void printInfo(){
        System.out.println("Line id: "+lineId);
        System.out.println("\nLine name: "+lineName);
        System.out.println("\nLine status: "+status);
        System.out.println("\nNumber of tasks: "+Tasks.size());
        System.out.println("\nTasks: ");
        for(Task task : Tasks){
            System.out.println("\nTask: " + task.getTaskId() + "   " + task.getStatus());
        }
    }

    public double getCompletion() {
        if(Tasks.isEmpty()){
            return 0.0;
        }
        int completedCount = 0;
        for(Task task : Tasks){
            if(task.getStatus() == Task.TaskStatus.COMPLETED)
                completedCount++;
        }
        double percentage = ((double)completedCount / getTasks().size()) * 100;
        return percentage;
    }
}