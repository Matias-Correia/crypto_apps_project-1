import java.nio.channels.SelectionKey;
import java.util.HashSet;

class Room{
  private String name = "";
  private HashSet<SelectionKey> users = new HashSet<>();

  Room(String name){
    this.name = name;
  }
  HashSet<SelectionKey> getUsers(){
    return users;
  }
  String getName(){
    return name;
  }
  void addUser(SelectionKey key){
    users.add(key);
  }
  void removeUser(SelectionKey key){
    users.remove(key);
  }
  boolean isEmpty(){
    return users.isEmpty();
  }
}