package DJT_Company;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class ManagerFeedback {
    private int lineId;
    private String lineName;
    private String feedback;
    private int rating;
    private LocalDate date;

    private static final String FEEDBACK_FILE = "manager_feedback.txt";

    public ManagerFeedback(int lineId, String lineName, String feedback, int rating, LocalDate date) {
        this.lineId = lineId;
        this.lineName = lineName;
        this.feedback = feedback;
        this.rating = rating;
        this.date = date;
    }

    public int getLineId() { return lineId; }
    public String getLineName() { return lineName; }
    public String getFeedback() { return feedback; }
    public int getRating() { return rating; }
    public LocalDate getDate() { return date; }

    public void saveFeedback() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FEEDBACK_FILE, true))) {
            bw.write(lineId + "," + lineName + "," + rating + "," + feedback.replaceAll(",", ";") + "," + date);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<ManagerFeedback> loadAllFeedbacks() {
        List<ManagerFeedback> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FEEDBACK_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 5);
                if (parts.length == 5) {
                    int lineId = Integer.parseInt(parts[0]);
                    String lineName = parts[1];
                    int rating = Integer.parseInt(parts[2]);
                    String feedback = parts[3];
                    LocalDate date = LocalDate.parse(parts[4]);
                    list.add(new ManagerFeedback(lineId, lineName, feedback, rating, date));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
