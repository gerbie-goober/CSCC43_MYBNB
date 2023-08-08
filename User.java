package mysql;

public class User{
    private int SIN;
    private String role;

    public User(int SIN, String role){
        this.SIN = SIN;
        this.role = role;
    }
    public int getSIN(){
        return SIN;
    }
    public String getRole(){
        return role;
    }

}
