package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }


  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }


  @PostMapping("/users/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    List <User> users = userService.getUsers();
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    User u = new User();
    for (int i=0; i<users.size(); i++){
      if (users.get(i).equals(userInput)){
        u = users.get(i);
      }
    }
    u.setStatus(UserStatus.ONLINE);
    u.setIsLoggedIn(true);
    userRepository.save(u);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(u);
  }

  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public User findUser(@PathVariable int id) {
    User user = userService.getUserById(id);
    return user;
    // return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public User updateUser(@PathVariable int id, @RequestBody User user) throws Exception{
    User updatedUser = userService.updateUser(id, user);
    return updatedUser;
  }

  @PostMapping("/users/logout/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void logoutUser(@PathVariable int id) {

    userService.logoutUser(id);
  }

}
