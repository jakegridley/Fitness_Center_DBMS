/*----------------------------------------------------------------------------
|  Class MemberData
|
|  Purpose: This helper class stores information relating
|           to a Member in the database.
|
*---------------------------------------------------------------------------*/
public class MemberData {
    private int memberID;
    private String name;
    private String phoneNum;
    private int acctBalance;
    private int moneySpent;
    private String membershipName;

    public int getAcctBalance() {
        return acctBalance;
    }

    public void setAcctBalance(int amnt) {
        this.acctBalance = amnt;
    }

    public int getMoneySpent() {
        return moneySpent;
    }

    public void setMoneySpent(int amnt) {
        this.moneySpent = amnt;
    }

    public String getMembershipName() {
        return membershipName;
    }

    public void setMembershipName(String n) {
        this.membershipName = n;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public int getMemberID() {
        return memberID;
    }

    public void setMemberID(int memberID) {
        this.memberID = memberID;
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
        retval += memberID + ", '" + name + "', '" + phoneNum + "', "
                + acctBalance + ", " + moneySpent + ", '" + membershipName + "')";
        return retval;
    }
}

