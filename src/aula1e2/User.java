package aula1e2;

class User{
 
  private String message = ""; //i need this to avoid responding to Ctrl + D on netcat
  private int id; 
  
  User(int id){
	  this.id = id;
  }

  public int getID() {
	  return id;
  }

  public String getMessage(){
    return message;
  }

  public void cleanMessage(){
    this.message = "";
  }
  public void addMessage(String message){
    this.message = this.message + message;
  }


}