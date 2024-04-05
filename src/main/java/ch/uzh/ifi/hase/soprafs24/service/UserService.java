package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
// import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.core.ApiFuture;
import com.google.cloud.Date;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  // private final UserRepository userRepository;
  private Map<String, User> docUsers = new HashMap<>();


  @Id
  @GeneratedValue
  private Long next=1L;

  // @Autowired
  // public UserService(@Qualifier("userRepository") UserRepository userRepository) {
  //   this.userRepository = userRepository;
  // }

  // public List<User> getUsers() {
  //   return this.userRepository.findAll();
  // }

  public User createUser(User newUser) throws Exception {
   

    Firestore db = FirestoreClient.getFirestore();

    DocumentReference docReference = db.collection("users").document("user");
    checkIfUserExists(newUser);
    Long id = next++;
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setIsLoggedIn(true);
    newUser.setId(id);
    docUsers.put(id.toString(), newUser);
    ApiFuture<WriteResult> apiFuture = docReference.set(docUsers);

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User findByUsername(String username) throws Exception{
    DocumentReference docRef = FirestoreClient.getFirestore().collection("users").document("user");

    User findUser = new User();

    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();

    if (document.exists()) {
      for (User user : docUsers.values()){
        if (user.getUsername().equals(username)){
          findUser = user;
        }
      }

    } else {
      System.out.println("No such document!");
    }

    return findUser;
  }

  public User getUserById(int id) throws ExecutionException, InterruptedException {
    DocumentReference docRef = FirestoreClient.getFirestore().collection("users").document("user");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();

    User findUser = new User();
    if (document.exists()) {
      for (User user : docUsers.values()){
        if (user.getId() == id){
          findUser = user;
        }
      }

    } else {
      System.out.println("No such document!");
    }
    return findUser;
  }

  public List<User> getUsers() throws InterruptedException, ExecutionException {
    DocumentReference docRef = FirestoreClient.getFirestore().collection("users").document("user");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();

    List<User> users = new ArrayList<>();

    if (document.exists()) {
      for (User user : docUsers.values()){
        users.add(user);
      }

    } else {
      System.out.println("No such document!");
    }
    return users;
  }



  // public User updateUser(int id, User user) throws Exception {
  //   User reqUser = this.getUserById(id);
  //   if (!user.getUsername().isBlank()){
  //     checkIfUserExists(user);
  //     reqUser.setUsername(user.getUsername());
  //   }
  //   if (user.getBirthDay() != null){
  //     reqUser.setBirthDay(user.getBirthDay());
  //   }
  //   reqUser.setPassword(this.getUserById(id).getPassword());
  //   reqUser.setToken(UUID.randomUUID().toString());
  //   reqUser.setStatus(UserStatus.ONLINE);
  //   reqUser.setIsLoggedIn(true);

  //   userRepository.save(reqUser);
  //   userRepository.flush();
  //   return reqUser;
  // }


  // @Transactional
  // public User logoutUser(int id){
  //   User u = this.getUserById(id);
  //   u.setStatus(UserStatus.OFFLINE);
  //   u.setIsLoggedIn(false);
  //   userRepository.save(u);
  //   userRepository.flush();

  //   return u;
  // }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws Exception 
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) throws Exception {
    User userByUsername = findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername.getUsername() != null) {
      
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    }
  }

}
