/*
 * CSC 460 - Database Design
 * Prog4.java
 * Professor Lester McCann
 * TAs: Zehnyu Qi and Danial Bazmandeh
 * Author: Edan Uccetta, Jake Gridley
 *
 *
 * Allows for various operations on an Oracle
 * SQL database regarding the information used for a sports center.
 *
 * The user can use a text-based interface to add and delete
 * members, courses, and packages.
 *
 * In addition, they can choose from a variety of queries:
 * - viewing the status of indebted members;
 * - viewing the schedule of members in November;
 * - viewing the working hours of trainers in December;
 * - viewing a total of one member's spending by trainer.
 *
 *
 */

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

public class Prog4 {

    // We need two static variables; the statement to use for JDBC, and the connection (so we can close it).
    private static Statement statement = null;
    private static Connection connection = null;

    /*----------------------------------------------------------------------------
    |  Method setupJDBC(String username, String password)
    |
    |  Purpose: This function uses the JDBC example code to create
    |           the SQL connection we use for Program 4.
    |
    |  Pre-condition: None.
    |
    |  Post-condition: If the username and password are correct, both statement
    |                  and connection will be setup properly.
    |
    |  Parameters:
    |      username -- The Oracle username to use on login.
    |      password -- The Oracle password associated with that username.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void setupJDBC(String username, String password) {
        final String oracleURL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

        try {
            Class.forName("oracle.jdbc.OracleDriver"); // Initialize the Oracle Driver.
        } catch (ClassNotFoundException e) {
            System.err.println("*** ClassNotFoundException:  " + "Error loading Oracle JDBC driver.  \n" + "\tPerhaps the driver is not on the Classpath?");
            System.exit(-1);
        }

        try {
            connection = DriverManager.getConnection(oracleURL, username, password); // Set up the connection.

        } catch (SQLException e) {
            System.err.println("*** SQLException:  " + "Could not open JDBC connection.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }

        try {
            // Finally, initialize the statement.
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Main function.
    // As usual, this function is mostly acting as the text output/input interface.
    // Besides that, it calls the other functions.
    public static void main(String[] args) {

        if (args.length >= 2) { // Get username and password
            setupJDBC(args[0], args[1]);
        } else {
            System.out.println("\nUsage:  java Prog4 <username> <password>\n" + "    where <username> is your Oracle DBMS" + " username,\n    and <password> is your Oracle" + " password.\n");
            System.exit(-1);
        }

        // Initial information printout.
        System.out.println("Welcome to Program 4.");
        System.out.println("What would you like to do?");
        System.out.println("1. Add or Delete a Member");
        System.out.println("2. Add or Delete a Course");
        System.out.println("3. Add, Update, or Delete a Package");
        System.out.println("4. View Indebted Members (query 1)");
        System.out.println("5. View Member Schedule (query 2)");
        System.out.println("6. View Trainer Hours (query 3)");
        System.out.println("7. Total Spend Per Instructor (query 4)");
        System.out.println("You may also type 'exit' to quit.");

        // Begin getting user input.
        Scanner userScanner = new Scanner(System.in);
        boolean validInput = false;

        /*
        For each potential case, we either simply
        call the appropriate query (if no further input is needed),
        or prompt the user for additional information first.
         */
        while (!validInput) {
            String response = userScanner.next();
            validInput = true;
            if (response.equalsIgnoreCase("1")) {
                System.out.println("Would you like to ADD or DELETE a member?");
                response = userScanner.next();
                if (response.equalsIgnoreCase("add")) {
                    addMember();
                } else if (response.equalsIgnoreCase("delete")) {
                    deleteMember();
                } else {
                    System.out.println("Invalid input. Please try again.");
                    validInput = false;
                }
            } else if (response.equalsIgnoreCase("2")) {
                System.out.println("Would you like to ADD or DELETE a course?");
                response = userScanner.next();
                if (response.equalsIgnoreCase("add")) {
                    addCourse();
                } else if (response.equalsIgnoreCase("delete")) {
                    deleteCourse();
                } else {
                    System.out.println("Invalid input. Please try again.");
                    validInput = false;
                }
            } else if (response.equalsIgnoreCase("3")) {
                System.out.println("Would you like to ADD, UPDATE, or DELETE a package?");
                response = userScanner.next();
                if (response.equalsIgnoreCase("add")) {
                    try {
                        addPackage();
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (response.equalsIgnoreCase("update")) {
                    try {
                        updatePackage();
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (response.equalsIgnoreCase("delete")) {
                    try {
                        deletePackage();
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (response.equalsIgnoreCase("4")) {
                queryNegativeBalances();
            } else if (response.equalsIgnoreCase("5")) {
                queryMemberSchedule();
            } else if (response.equalsIgnoreCase("6")) {
                queryAllTrainerSchedules();
            } else if (response.equalsIgnoreCase("7")) {
                queryMemberSpending();
            } else if (response.equalsIgnoreCase("exit")) {
                endProgram();
            } else {
                validInput = false;
                System.out.println("Invalid input. Please try again.");
            }
        }

        // If we've gotten this far, we must have executed a query or action. End the program now too.
        endProgram();
    }

    /*----------------------------------------------------------------------------
    |  Method endProgram()
    |
    |  Purpose: This function prints a farewell message, closes the DBMS connections,
    |           and terminates the program.
    |
    |  Pre-condition: None.
    |
    |  Post-condition: The connections are closed and the program ends.
    |
    |  Parameters:
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void endProgram() {
        System.out.println(" --- Thanks for using Program 4 --- ");
        //try {
        //statement.close();
        //connection.close();
//        } catch (SQLException e) {
//            System.out.println("Couldn't close the DBMS connection!");
//            e.printStackTrace();
//        }
        System.exit(0);
    }

    /*----------------------------------------------------------------------------
    |  Method queryNegativeBalances()
    |
    |  Purpose: This function uses the DBMS connection to display the list
    |           of members who owe money (i.e. account balances under 0).
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void queryNegativeBalances() {
        try {
            System.out.println("Members with Negative Balances");
            System.out.println("NAME - PHONE #");
            // Query: Select the name and telephone number of each member with a negative balance.
            ResultSet courses = statement.executeQuery("SELECT name, telephone_no FROM lexc.member WHERE acc_balance<0");
            while (courses.next()) {
                System.out.println(courses.getString("name") + " - " + courses.getString("phone"));
            }
        } catch (Exception e) {
            System.out.println("Error in Query One");
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------------
    |  Method queryMemberSchedule()
    |
    |  Purpose: This function uses the DBMS connection to display a specified
    |           member's schedule in November of the current year.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void queryMemberSchedule() {
        try {
            listMembers();
            // Prompt the user for the member name desired.
            Scanner userScanner = new Scanner(System.in);
            System.out.println("Please provide the ID of the member whose schedule to check.");
            String inputID = userScanner.next();

            ResultSet correspondingMember = statement.executeQuery("SELECT lexc.member.name FROM lexc.member WHERE member_id=" + Integer.parseInt(inputID));
            while (!correspondingMember.next()) {
                System.out.println("Could not find a member by that ID!");
                inputID = userScanner.next();
                correspondingMember = statement.executeQuery("SELECT lexc.member.name FROM lexc.member WHERE member_id=" + Integer.parseInt(inputID));
            }

            correspondingMember.beforeFirst(); // We need to reset after checking if empty.

            String name = "";
            while (correspondingMember.next()) {
                name = correspondingMember.getString("name");
            }


            System.out.println(name + "'s Schedule during November:");
            int year = Year.now().getValue();
            // Query: Select the course data from each course associated with a package in a subscription that the member has enrolled in.
            ResultSet membersCourses = statement.executeQuery("SELECT lexc.course.name, lexc.course.start_date, lexc.course.end_date, lexc.course.start_time, lexc.course.duration FROM " + "(" + "(" + "(" + "(" + "lexc.member JOIN lexc.subscription ON lexc.member.member_id=lexc.subscription.member_id" + ")" + " JOIN lexc.package ON lexc.subscription.package_id=lexc.package.package_id" + ")" + " JOIN lexc.coursepackage ON lexc.package.package_id=lexc.coursepackage.package_id" + ")" + " JOIN lexc.course ON lexc.coursepackage.course_id=lexc.course.course_id" + ")" + " WHERE lexc.member.member_id=" + inputID);
            while (membersCourses.next()) {
                // In this case, we need to check some data repeatedly, so we use the CourseData class.
                // We set up the CourseData based on the query, then check if it's within the range.
                CourseData targetCourse = new CourseData();
                targetCourse.setCourseName(membersCourses.getString("name"));
                targetCourse.setStartDate(membersCourses.getDate("start_date"));
                targetCourse.setStartTime(membersCourses.getString("start_time"));
                targetCourse.setEndDate(membersCourses.getDate("end_date"));
                targetCourse.setDuration(membersCourses.getFloat("duration"));
                // If the course started this year or earlier, and it started in November or earlier of that year,
                // and it either ends next year, or ends this year during November or later,
                // then it's in November - print it as part of the schedule.
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(targetCourse.getStartDate());
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(targetCourse.getEndDate());
                if (startCalendar.get(Calendar.YEAR) <= year && startCalendar.get(Calendar.MONTH) <= 10 && ((endCalendar.get(Calendar.YEAR) == year && endCalendar.get(Calendar.MONTH) >= 10) || endCalendar.get(Calendar.YEAR) > year))
                    System.out.println(targetCourse.getCourseName() + " from " + targetCourse.getStartDate() + " to " + targetCourse.getEndDate() + ", starting at " + targetCourse.getStartTime() + " and ending at " + addTime(targetCourse.getStartTime(), targetCourse.getDuration()));
            }
        } catch (Exception e) {
            System.out.println("Error in Query Two");
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------------
    |  Method queryTrainerSchedule()
    |
    |  Purpose: This function uses the DBMS connection to display each
    |           trainer's schedule for December of the current year.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void queryAllTrainerSchedules() {
        try {
            HashMap<String, ArrayList<CourseData>> schedulingMap = new HashMap<>();
            // Map from trainer names to a list of courses that trainer teaches.
            // Query: Select the trainer name and course data of every course taught by that trainer.
            ResultSet trainerSchedules = statement.executeQuery("SELECT lexc.trainer.name AS trainerName, lexc.course.name AS courseName, lexc.course.start_time, lexc.course.duration, lexc.course.start_date, lexc.course.end_date FROM " + "(" + "lexc.trainer JOIN lexc.course ON lexc.course.trainer_id=lexc.trainer.trainer_id" + ")");
            while (trainerSchedules.next()) {
                // Setup a CourseData based on the discovered courses, and add them to the list of courses taught by
                // the found trainer.
                String rowsTrainer = trainerSchedules.getString("trainerName");
                if (!schedulingMap.containsKey(rowsTrainer)) {
                    schedulingMap.put(rowsTrainer, new ArrayList<>());
                }
                CourseData nextCourse = new CourseData();
                nextCourse.setCourseName(trainerSchedules.getString("courseName"));
                nextCourse.setStartTime(trainerSchedules.getString("start_time"));
                nextCourse.setDuration(trainerSchedules.getFloat("duration"));
                nextCourse.setStartDate(trainerSchedules.getDate("start_date"));
                nextCourse.setEndDate(trainerSchedules.getDate("end_date"));
                schedulingMap.get(rowsTrainer).add(nextCourse);
            }
            int year = Year.now().getValue();
            // For each trainer teaching at least one course...
            for (String trainerName : schedulingMap.keySet()) {
                System.out.println(trainerName + "'s schedule for December:");
                // For each course that trainer teaches...
                for (CourseData nextCourse : schedulingMap.get(trainerName)) {
                    // If it started this year or earlier, and it either ended in December of this year or
                    // ends in a future year, it's part of December.
                    // Print it as part of the trainer's December schedule.
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.setTime(nextCourse.getStartDate());
                    Calendar endCalendar = Calendar.getInstance();
                    endCalendar.setTime(nextCourse.getEndDate());
                    if (startCalendar.get(Calendar.YEAR) <= year && startCalendar.get(Calendar.MONTH) <= 11 && ((endCalendar.get(Calendar.YEAR) == year && endCalendar.get(Calendar.MONTH) >= 11) || endCalendar.get(Calendar.YEAR) > year))
                        System.out.println(nextCourse.getCourseName() + " from " + nextCourse.getStartDate() + " to " + nextCourse.getEndDate() + ", starting at " + nextCourse.getStartTime() + " and ending at " + addTime(nextCourse.getStartTime(), nextCourse.getDuration()));
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Error in Query 3");
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------------
    |  Method queryMemberSpending()
    |
    |  Purpose: This function uses the DBMS connection to display a specified
    |           member's spending, shown per trainer spent upon.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void queryMemberSpending() {
        try {
            listMembers();
            // Get user input for the desired member name, and find that member, repeating until we do.
            Scanner userScanner = new Scanner(System.in);
            System.out.println("Please provide the ID of the member whose spending to check.");
            String inputID = userScanner.next();

            ResultSet correspondingMember = statement.executeQuery("SELECT name FROM lexc.member WHERE member_id=" + inputID);
            while (!correspondingMember.next()) {
                System.out.println("Could not find a member by that ID!");
                inputID = userScanner.next();
                correspondingMember = statement.executeQuery("SELECT name FROM lexc.member WHERE member_id=" + inputID);
            }

            correspondingMember.beforeFirst(); // We need to reset after checking if empty.

            String name = "";
            while (correspondingMember.next()) {
                name = correspondingMember.getString("name");
            }

            // Map from trainer names to money spent.
            HashMap<String, Integer> spendingMap = new HashMap<>();
            // Select the trainer name and transaction amount from...  deep breath...
            // the courses offered by packages for which that desired member made transactions.
            ResultSet transactionToTrainerForMember = statement.executeQuery("SELECT lexc.trainer.name, lexc.transaction.amount FROM " + "(" + "(" + "(" + "(" + "(lexc.member JOIN lexc.transaction ON lexc.member.member_id=lexc.transaction.member_id)" + " JOIN lexc.package ON lexc.package.package_id=lexc.transaction.package_id)" + " JOIN lexc.coursepackage ON lexc.coursepackage.package_id=lexc.package.package_id)" + " JOIN lexc.course ON lexc.coursepackage.course_id=lexc.course.course_id)" + " JOIN lexc.trainer ON lexc.trainer.trainer_id=lexc.course.trainer_id)" + " WHERE lexc.member.member_id=" + inputID);
            while (transactionToTrainerForMember.next()) {
                // For each of those name-amount pairs, if that trainer already had recorded spending,
                // just add to that number. Otherwise, set up the amount.
                String trainerName = transactionToTrainerForMember.getString("name");
                if (spendingMap.containsKey(trainerName)) {
                    spendingMap.put(trainerName, spendingMap.get(trainerName) + transactionToTrainerForMember.getInt("amount"));
                } else {
                    spendingMap.put(trainerName, transactionToTrainerForMember.getInt("amount"));
                }
            }
            // Finally, print out that data.
            System.out.println("Spending Per Instructor for " + name);
            for (String trainerName : spendingMap.keySet()) {
                System.out.println(trainerName + ": $" + spendingMap.get(trainerName) + " spent");
            }
        } catch (Exception e) {
            System.out.println("Error in Query Four");
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------------
    |  Method deleteCourse()
    |
    |  Purpose: This function displays all the courses in the database to the user,
    |           and prompts the user to enter the course_id of the course to be deleted.
    |           It then prints contact info for all members enrolled in the course if it has
    |           not already ended. After this, the course is deleted from the DB.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: The course has been deleted from the DB.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void deleteCourse() {
        Scanner scn = new Scanner(System.in);
        TreeMap<Integer, Date> courses_data = new TreeMap<>();
        try {
            ResultSet courses = statement.executeQuery("select * from lexc.Course");
            if (courses != null) {
                System.out.println("Select one of the following Course IDs to delete the course:");
                System.out.printf("%-12s%-20s%-15s%n", "Course ID", "Course Name", "End Date");
                System.out.println("-------------------------------------------------------");
                while (courses.next()) {
                    int curID = courses.getInt("course_id");
                    Date curDate = courses.getDate("end_date");
                    courses_data.put(curID, curDate);
                    System.out.printf("%-12d%-20s%-15s%n", curID, courses.getString("name"), curDate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error listing the courses.");
        }
        int course_id = Integer.parseInt(scn.nextLine());
        String dateString = LocalDate.now().toString();
        Date curDate = Date.valueOf(dateString);
        Date courseEndDate = courses_data.get(course_id);
        int comp = compareDates(curDate, courseEndDate);
        if (comp > 0) {
            System.out.println("This course has not ended and still has the following active members enrolled:");
            findPackageIDs(course_id);
        }
        // delete the course
        try {
            statement.executeQuery("delete from lexc.Course where course_id = " + course_id);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error deleted the course " + course_id);
        }
        System.out.println("The course " + course_id + " has been deleted");

    }


    /*----------------------------------------------------------------------------
    |  Method findPackageIDs(int course_id)
    |
    |  Purpose: This function finds all the package_ids from the CoursePackage table
    |           where the parameter is equal to the course_id in CoursePackage. This
    |           allows the program to find the members enrolled in the given course.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: int course_id: - This parameter is used in the query string to find the
    |                               correct Packages that this course is a part of.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void findPackageIDs(int course_id) {
        try {
            ResultSet pack_ids = statement.executeQuery("select package_id from lexc.CoursePackage where course_id = " + course_id);
            if (pack_ids != null) {
                while (pack_ids.next()) {
                    int curPID = pack_ids.getInt("package_id");
                    getMemberIDs(curPID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error printing the members enrolled in course " + course_id);
        }
    }

    /*----------------------------------------------------------------------------
    |  Method getMemberIDs()
    |
    |  Purpose: This function finds all the member_ids of the members that are enrolled in
    |           the specific package given as a parameter.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: int package_id: - This parameter is used in the query string to find the
    |                                correct member_ids that are enrolled in this package.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void getMemberIDs(int package_id) {
        try {
            ResultSet member_ids = statement.executeQuery("select member_id from lexc.Subscription where package_id = " + package_id);
            if (member_ids != null) {
                System.out.printf("%-12s%-20s%-20s%n", "Member ID", "Name", "Phone Num");
                while (member_ids.next()) {
                    int curMID = member_ids.getInt("member_id");
                    printMemberInfo(curMID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error getting member ids associated with the package " + package_id);
        }
    }

    /*----------------------------------------------------------------------------
    |  Method printMemberInfo(int member_id)
    |
    |  Purpose: This function prints the name and phone num of a single member
    |           associated with the member_id given as a parameter.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: int member_id: - This parameter is used in the query string to find the
    |                                attributes of the member in the member table with this id.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void printMemberInfo(int member_id) {
        try {
            ResultSet members = statement.executeQuery("select * from lexc.Member where member_id = " + member_id);
            if (members != null) {
                while (members.next()) {
                    System.out.printf("%-12d%-20s%-20s%n", members.getInt("member_id"), members.getString("name"), members.getString("telephone_num"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error getting member info associated with the member " + member_id);
        }
    }

    /*----------------------------------------------------------------------------
    |  Method addCourse()
    |
    |  Purpose: This function uses the DBMS connection and user input
    |           to add a course to the lexc DBMS table.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: A new course is added to the DBMS table.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void addCourse() {
        // Get user input, and use it to populate a CourseData.
        // CourseData has an insertString function that makes insertion easier.
        Scanner scn = new Scanner(System.in);
        CourseData newCourse = new CourseData();
        newCourse.setCourseID(getNextID("Course"));
        System.out.println("Please input the name of the course to add:");
        String name = scn.nextLine();
        newCourse.setCourseName(name);
        System.out.println("Please input the start time in the form 'H:MM PM/AM':");
        String start_time = scn.nextLine();
        newCourse.setStartTime(start_time);
        System.out.println("Enter a duration for the course:");
        int duration = Integer.parseInt(scn.nextLine());
        newCourse.setDuration(duration);
        System.out.println("Enter a start date for the course in the form 'YYYY-MM-DD':");
        String start_date = scn.nextLine();
        newCourse.setStartDate(Date.valueOf(start_date));
        System.out.println("Enter a end date for the course in the form 'YYYY-MM-DD':");
        String end_date = scn.nextLine();
        newCourse.setEndDate(Date.valueOf(end_date));
        System.out.println("Enter the maximum number of members that can take this course:");
        int maxMems = Integer.parseInt(scn.nextLine());
        newCourse.setMaxMembers(maxMems);
        System.out.println("Select a Trainer ID from this list corresponding to the trainer that will teach this course:");
        listTrainers();
        int trainer_id = Integer.parseInt(scn.nextLine());
        newCourse.setTrainerID(trainer_id);

        try {
            // Query: Inserting the desired course into the table.
            statement.executeQuery("insert into lexc.Course values " + newCourse.insertString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error inserting this course into the table.");
        }
    }

    /*----------------------------------------------------------------------------
    |  Method listTrainers()
    |
    |  Purpose: This function uses the DBMS connection to display a list
    |           of all current trainers in the database.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void listTrainers() {
        try {
            // Query: Select all trainers.
            ResultSet trainers = statement.executeQuery("select * from lexc.trainer");
            if (trainers != null) {
                System.out.printf("%-12s\t%-20s%n", "Trainer ID", "Name");
                System.out.println("---------------------------------------------");
                while (trainers.next()) {
                    System.out.printf("%-12d\t%-20s%n", trainers.getInt("trainer_id"), trainers.getString("name"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error listing all the trainers in the database.");
        }
    }

    /*----------------------------------------------------------------------------
    |  Method listMembers()
    |
    |  Purpose: This function uses the DBMS connection to display a list
    |           of all current members in the database.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void listMembers() {
        try {
            // Query: Get all members.
            ResultSet members = statement.executeQuery("SELECT * from lexc.Member");
            // Next, we list off all member.
            if (members != null) {
                System.out.println();
                System.out.printf("%-10s\t%-20s\t%-10s\t%-14s%n", "Member ID", "Name", "Phone Num", "Acct Balance");
                System.out.println("--------------------------------------------------------------------------");
                while (members.next()) {
                    int curMemID = members.getInt("member_id");
                    String curName = members.getString("name");
                    String phoneNum = members.getString("telephone_no");
                    int acct_balance = members.getInt("acc_balance");
                    System.out.printf("%-10d\t%-20s\t%-10s\t%-14d%n", curMemID, curName, phoneNum, acct_balance);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error listing all the trainers in the database.");
        }
    }

    /*----------------------------------------------------------------------------
    |  Method addMember()
    |
    |  Purpose: This function uses the DBMS connection with user input
    |           to add a member to the database.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: A new member is created in the database.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void addMember() {
        // Get user input, and use it to set up a MemberData.
        MemberData newMember = new MemberData();
        newMember.setMemberID(getNextID("Member"));
        Scanner userScanner = new Scanner(System.in);
        System.out.println("Please input the name of the member to add:");
        String name = userScanner.nextLine();
        newMember.setName(name);
        System.out.println("Please input " + name + "'s phone number:");
        String number = userScanner.nextLine();
        newMember.setPhoneNum(number);
        newMember.setAcctBalance(0);
        newMember.setMoneySpent(0);
        newMember.setMembershipName("");
        try {
            // Query: Get all packages.
            // Next, we display all the packages as needed for subscription.
            ResultSet packages = statement.executeQuery("SELECT * from lexc.Package");
            TreeMap<Integer, Integer> package_prices = new TreeMap<>();
            if (packages != null) {
                System.out.println();
                System.out.printf("%-12s\t%-20s\t%-8s%n", "Package ID", "Package Name", "Price");
                System.out.println("------------------------------------------------");
                while (packages.next()) {
                    int curID = packages.getInt("package_id");
                    int curPrice = packages.getInt("price");
                    package_prices.put(curID, curPrice);
                    System.out.printf("%-12d\t%-20s\t%-8d%n", curID, packages.getString("name"), curPrice);
                }
            }
            // Finally, the user chooses a package.
            // Not only do we add the member, we also need to add a corresponding Subscription.
            System.out.println("Enter the Package ID of your choosing:");
            int package_id = Integer.parseInt(userScanner.nextLine());
            int price = package_prices.get(package_id);
            newMember.setAcctBalance(newMember.getAcctBalance() + price);
            statement.execute("INSERT INTO lexc.Member VALUES " + newMember.insertString());
            statement.execute("INSERT INTO lexc.Subscription VALUES (" + newMember.getMemberID() + ", " + package_id + ")");
            System.out.println("Member added successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*----------------------------------------------------------------------------
    |  Method deleteMember()
    |
    |  Purpose: This function uses the DBMS connection to delete a
    |           member as requested by the user.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: The requested member is deleted from the database.
    |
    |  Parameters: None.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void deleteMember() {
        try {
            listMembers();
            System.out.println("Enter a member ID to delete:");
            // Get the user's desired ID to delete.
            Scanner scn = new Scanner(System.in);
            int memID = Integer.parseInt(scn.nextLine());

            // Call some extra functions to assist in removing checkouts and verifying deletion.
            checkEquipmentCheckouts(memID);
            boolean deletable = checkTransactionHistory(memID);
            removeSubscriptions(memID);
            if (deletable) {
                // Query: Delete the member.
                statement.execute("DELETE from lexc.Member where member_id = " + memID);
                System.out.println("Member deleted successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in deleting a member");
        }
    }

    /*----------------------------------------------------------------------------
    |  Method removeSubscriptions(int memID)
    |
    |  Purpose: This function uses the DBMS connection to remove all subscriptions
    |           associated with the input member ID.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: Subscriptions associated with the member are deleted.
    |
    |  Parameters: The member ID to delete based off of.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void removeSubscriptions(int memID) {
        try {
            // Query: Delete all subscriptions with matching member ID.
            statement.executeQuery("DELETE from lexc.Subscription where member_id = " + memID);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in removing member " + memID + "'s subscriptions");
        }
    }

    /*----------------------------------------------------------------------------
    |  Method checkTransactionHistory(int memID)
    |
    |  Purpose: This function uses the DBMS connection to check
    |           the transaction history of members, particularly in order
    |           to determine whether or not the member can be deleted.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: The member ID to analyze.
    |
    |  Returns: Whether or not the member can be deleted.
    |
    *---------------------------------------------------------------------------*/
    private static boolean checkTransactionHistory(int memID) {
        try {
            // Query: Get the name and account balance of the desired member.
            ResultSet member = statement.executeQuery("select Acc_Balance, name from lexc.Member where member_id = " + memID);
            if (member != null) {
                while (member.next()) {
                    // If the member owes money, display an explanatory message and refuse to delete them.
                    // Otherwise, they may be deleted.
                    int amountOwed = member.getInt("Acc_Balance");
                    if (amountOwed > 0) {
                        String name = member.getString("name");
                        System.out.println("The member '" + name + "' owes a balance of $" + amountOwed);
                        System.out.println("This member cannot be deleted until the balance is paid off");
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not find the member with the id - " + memID);
        }
        return true;
    }

    /*----------------------------------------------------------------------------
    |  Method checkEquipmentCheckouts(int memID)
    |
    |  Purpose: This function uses the DBMS connection to reduce the stock
    |           of items that were still out when the member was deleted.
    |           (We assume they won't be returned.)
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: The stock of equipment that the to-be-deleted member had
    |                  out will be decreased.
    |
    |  Parameters: The member ID to check.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void checkEquipmentCheckouts(int memID) {
        try {
            // Query: Select all checkouts under the desired member.
            ResultSet checkouts = statement.executeQuery("SELECT * from lexc.checkout where member_id = " + memID);
            if (checkouts != null) {
                while (checkouts.next()) {
                    // If the item hasn't been returned...
                    if (checkouts.getTimestamp("In_time") == null) {
                        // Decrease its stock.
                        decreaseItemStock(checkouts.getInt("Item_ID"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in searching for checkouts by member with id - " + memID);
        }
    }

    /*----------------------------------------------------------------------------
    |  Method decreaseItemStock(int itemID)
    |
    |  Purpose: This function uses the DBMS connection to decrease the stock of
    |           the desired item by 1.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: The item with the corresponding ID will have its stock
    |                  reduced by 1.
    |
    |  Parameters: The item ID to decrease stock of.
    |
    |  Returns: None.
    |
    *---------------------------------------------------------------------------*/
    private static void decreaseItemStock(int itemID) {
        try {
            // Query: Reduce the stock of the desired item.
            statement.executeQuery("update lexc.Equipment set stock = stock - 1 where item_id = " + itemID);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error Decreasing the missing item's stock");
        }
    }

    /*----------------------------------------------------------------------------
    |  Method getNextID(String table)
    |
    |  Purpose: This function uses the DBMS connection to get the first open ID on
    |           a table. In its current form, this is only used for memberIDs.
    |
    |  Pre-condition: DBMS connection is valid.
    |
    |  Post-condition: None.
    |
    |  Parameters: The table to query.
    |
    |  Returns: The next available ID.
    |
    *---------------------------------------------------------------------------*/
    private static int getNextID(String table) {
        ResultSet max_id = null;
        int retval = -1;
        try {
            // Query: Get the max found member_id in the desired table.
            max_id = statement.executeQuery("SELECT MAX(" + table + "_id) from lexc." + table);
            if (max_id != null) {
                while (max_id.next()) {
                    retval = max_id.getInt("MAX(" + table + "_id)");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error getting the max member_id from the Member table");
        }
        return retval + 1;
    }

    /*----------------------------------------------------------------------------
    |  Method addTime(Time timeIn, float hourVal)
    |
    |  Purpose: This helper function calculates the result of adding hourVal
    |           hours to timeIn. It returns the result as a String.
    |
    |  Pre-condition: None.
    |
    |  Post-condition: None.
    |
    |  Parameters: The time to add to, and the hours to add.
    |
    |  Returns: A string corresponding to the time outputted.
    |
    *---------------------------------------------------------------------------*/
    private static String addTime(Time timeIn, float hourVal) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeIn);
        calendar.add(Calendar.HOUR_OF_DAY, (int) Math.floor(hourVal));
        calendar.add(Calendar.MINUTE, (int) (hourVal % 1));
        return calendar.get(Calendar.HOUR_OF_DAY) + ":00";
    }


    // Adding, Deleting, Updating Packages

    public static void deletePackage() throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Which package do you want to delete?");
        String query1 = "SELECT Package_ID,Name FROM lexc.Package";

        ResultSet answer = Prog4.statement.executeQuery(query1);
        while (answer.next()) {
            System.out.println(answer.getString(1));
        }

        String delPack = scan.nextLine();

        if (checkDatesPackage(delPack)) {
            if (checkRegistration(delPack)) deleteWhole(delPack);
            else System.out.println("People are currently enrolled in that class");
        } else System.out.println("Package currenlty has classes which are ongoing");

        scan.close();
    }

    public static boolean checkDatesPackage(String Package) throws SQLException {
        boolean valid = true;
        String query1 = "SELECT course_id FROM lexc.coursepackage " + "WHERE package_id=" + Package;
        ResultSet answer = statement.executeQuery(query1);
        ArrayList<Integer> courseIdsToCheck = new ArrayList<>();
        if (answer != null) {
            while (answer.next()) {
                courseIdsToCheck.add(answer.getInt("course_id"));
            }
            for (Integer courseID : courseIdsToCheck) {
                valid = checkDates(courseID);
                if (valid == false) {
                    break;
                }
            }
        } else System.out.println("Error printing the members enrolled in course " + Package);
        return valid;
    }

    private static boolean checkDates(int courseID) throws SQLException {
        long millis = System.currentTimeMillis();
        Date curDate = new Date(millis);
        String query1 = "SELECT End_Date FROM LEXC.COURSE " + "WHERE course_id = %s";
        query1 = String.format(query1, courseID);
        ResultSet answer = statement.executeQuery(query1);
        answer.next();
        Date endDate = answer.getDate(1);
        return curDate.before(endDate);

    }

    public static boolean checkRegistration(String Package) throws SQLException {
        String query1 = "SELECT COUNT(*) FROM lexc.Subscription" + " WHERE Package_ID = %s";
        query1 = String.format(query1, Package);
        ResultSet answer = Prog4.statement.executeQuery(query1);
        answer.next();
        if (answer.getInt(1) != 0) return false;
        else return true;

    }

    private static void deleteWhole(String Package) throws SQLException {
        // TODO Auto-generated method stub
        String query = "DELETE FROM lexc.CoursePackage WHERE Package_ID = %s";
        query = String.format(query, Package);
        Prog4.statement.executeQuery(query);

        String query2 = "DELETE FROM lexc.Package WHERE Package_ID = %s";
        query2 = String.format(query2, Package);
        Prog4.statement.executeQuery(query2);


    }


    public static void updatePackage() throws SQLException {
        Scanner scan = new Scanner(System.in);  // Create a Scanner object
        ResultSet answer = null;
        String query1 = "SELECT Package_ID,Name FROM lexc.Package";

        System.out.println("Avaiable packages.....");
        answer = Prog4.statement.executeQuery(query1);
        while (answer.next()) {
            System.out.println(answer.getString(1));
        }
        System.out.println("Please select a Package you would want to change");
        String result2 = scan.nextLine();
        if (Prog4.checkDatesPackage(result2)) {
            if (Prog4.checkRegistration(result2)) {
                String query2 = "SELECT Course_ID,Name FROM lexc.Course" + " WHERE End_Date > SYSDATE";
                answer = Prog4.statement.executeQuery(query2);
                answer.next();
                System.out.println("Avaiable courses to add/delete to the Package....");
                while (answer.next()) {
                    System.out.println(answer.getString(1) + "\t" + answer.getString(2));
                }
                System.out.println("Enter CourseID which you would like to add/delete?");
                String result4 = scan.nextLine();


                System.out.println("Do you want to delete or add onto this package?");
                String result3 = scan.nextLine();
                if (result3.toLowerCase() == "add") {
                    updateHelper(result2, result4, "add");

                } else if (result3.toLowerCase() == "delete") {
                    updateHelper(result2, result4, "delete");
                }
            } else System.out.println("People are currently enrolled in this package");


        } else System.out.println("You cannot change this package because all classes involved are terminated");

        scan.close();

    }

    private static void updateHelper(String packageID, String courseID, String str) throws SQLException {
        // TODO Auto-generated method stub
        if (str.equals("add")) {
            String query1 = "INSERT INTO CoursePackage(Package_ID,Course_ID) VALUES " + "VALUES (%s,'%s')";
            query1 = String.format(query1, packageID, courseID);
            statement.executeQuery(query1);
        } else {
            String query1 = "DELETE FROM lexc.CoursePackage WHERE Package_ID = %s AND Course_ID = %s";
            query1 = String.format(query1, packageID, courseID);
            statement.executeQuery(query1);

        }
    }

    public static void addPackage() throws SQLException {
        Scanner scan = new Scanner(System.in);

        System.out.println("What do you want the title of the package to be?");
        String title = scan.nextLine();
        System.out.println("What do you want the price of the package to be?");
        int price = scan.nextInt();
        scan.nextLine();

        String query4 = "SELECT COUNT(*) FROM lexc.Package";
        ResultSet answer = Prog4.statement.executeQuery(query4);
        answer.next();
        int newID = Integer.valueOf(answer.getString(1)) - 1;
        String query5;
        query5 = "INSERT INTO Package(Package_ID,Name,Price) " + "VALUES (%s,'%s',%s)";
        query5 = String.format(query5, newID, title, price);
        Prog4.statement.executeQuery(query5);


        String query1 = "SELECT Course_ID,Name FROM lexc.Course" + " WHERE End_Date > SYSDATE";
        answer = Prog4.statement.executeQuery(query1);
        System.out.println("Avaiable courses to build a Package");
        while (answer.next()) {
            System.out.println(answer.getString(1) + "\t" + answer.getString(2));
        }
        System.out.println("Please enter Course IDs you would want to package together sepearted by commas");
        String results = scan.nextLine();
        String[] resultsArr = results.split(",");

        for (int i = 0; i < resultsArr.length; i++) {
            String query3 = "INSERT INTO lexc.CoursePackage(Package_ID,Course_ID) " + "VALUES (%s,%s)";
            query3 = String.format(query3, newID, resultsArr[i]);
            Prog4.statement.executeQuery(query3);
        }

        scan.close();


    }

    /*----------------------------------------------------------------------------
    |  Method compareDates(Date d1, Date d2)
    |
    |  Purpose: This function compares two dates to see which comes first, if d1 comes
    |           first -1 is return otherwise 1 is returned.
    |
    |  Pre-condition: Parameters are not null.
    |
    |  Post-condition: None.
    |
    |  Parameters: Date d1, Date d2: - two dates to be compared.
    |
    |  Returns: int: -1 if d1 is before d2, otherwise 1.
    |
    *---------------------------------------------------------------------------*/
    private static int compareDates(Date d1, Date d2) {
        String date1 = d1.toString();
        String date2 = d2.toString();
        String[] d1arr = date1.split("-");
        String[] d2arr = date2.split("-");
        if (Integer.parseInt(d1arr[0]) < Integer.parseInt(d2arr[0])) {
            return -1;
        } else if (Integer.parseInt(d1arr[1]) < Integer.parseInt(d2arr[1])) {
            return -1;
        } else if (Integer.parseInt(d1arr[2]) < Integer.parseInt(d2arr[2])) {
            return -1;
        }
        return 1;
    }

}


