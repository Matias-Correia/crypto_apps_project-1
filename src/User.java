class User{
 
  private String message = ""; //i need this to avoid responding to Ctrl + D on netcat

  User(){
   
  }

 
  String getMessage(){
    return message;
  }

  void cleanMessage(){
    this.message = "";
  }
  void addMessage(String message){
    this.message = this.message + message;
  }


}