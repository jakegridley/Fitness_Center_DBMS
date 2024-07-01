/*----------------------------------------------------------------------------
|  Class CourseData
|
|  Purpose: This helper class stores information that corresponds to
|           a Course in the database.
|
*---------------------------------------------------------------------------*/

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;

public class CourseData {
    // Class Variables
    private int courseID;
    private String courseName;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    //private DayOfWeek startDay;
    private float duration;
    private int maxMembers;
    private int trainerID;

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int ID) {
        this.courseID = ID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        // We accidentally missed including day of the week when setting up the SQL database.
        //String dayPart = startTime.split(" ")[0];
        String timePart = startTime.split(" ")[0];
        //this.startDay = DayOfWeek.valueOf(dayPart);
        this.startTime = Time.valueOf(timePart + ":00");
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public int getTrainerID() {
        return trainerID;
    }

    public void setTrainerID(int trainerID) {
        this.trainerID = trainerID;
    }

    /*----------------------------------------------------------------------------
    |  Method insertString()
    |
    |  Purpose: This function returns a set of values as a well-formatted
    |           string for use in a SQL Insert Into statement.
    |
    |  Pre-condition: None.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: The CSV values as needed for INSERT INTO.
    |
    *---------------------------------------------------------------------------*/
    public String insertString() {
        String retval = "(";
        retval += courseID + ", '" + courseName + "', '" + startTime + "', "
                + duration + ", DATE '" + startDate + "', DATE '" + endDate + "', " + maxMembers
                + ", " + trainerID + ")";
        return retval;
    }
}
