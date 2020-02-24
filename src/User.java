class User{
  private String nickname = "";
  private State state = State.init;
  private Room room;
  private String message = ""; //i need this to avoid responding to Ctrl + D on netcat

  User(){
    this.state = State.init;
  }

  String getNickname(){
    return this.nickname;
  }
  State getState(){
    return this.state;
  }
  Room getRoom(){
    return this.room;
  }
  String getMessage(){
    return message;
  }

  void setNickname(String nickname){
    this.nickname = nickname;
  }
  void setState(State state){
    this.state = state;
  }
  void setRoom(Room room){
    this.room = room;
  }
  void cleanMessage(){
    this.message = "";
  }
  void addMessage(String message){
    this.message = this.message + message;
  }

  boolean isInRoom(){
    return this.getRoom() != null ? true : false;
  }

}