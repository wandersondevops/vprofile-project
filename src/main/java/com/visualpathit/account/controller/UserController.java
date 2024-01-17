package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.ProducerService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.MemcachedUtils;
import com.visualpathit.account.validator.UserValidator;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
/**{@author waheedk}*/
@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;
    
    @Autowired
    private ProducerService producerService;
    
    /** {@inheritDoc} */
    @GetMapping(value = "/registration")
    public final String registration(final Model model) {
        model.addAttribute("userForm", new User());
             	return "registration";
      }
    /** {@inheritDoc} */
    @PostMapping(value = "/registration")
    public final String registration(final @ModelAttribute("userForm") User userForm, 
    	final BindingResult bindingResult, final Model model) {
    	
        userValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        System.out.println("User PWD:"+userForm.getPassword());
        userService.save(userForm);

        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/welcome";
    }
    /** {@inheritDoc} */
    @GetMapping(value = "/login")
    public final String login(final Model model, final String error, final String logout) {
        System.out.println("Model data"+model.toString());
    	if (error != null){
            model.addAttribute("error", "Your username and password is invalid.");
        }
        if (logout != null){
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }
    /** {@inheritDoc} */
    @GetMapping(value = { "/", "/welcome"})
    public final String welcome(final Model model) {
        return "welcome";
    }
    /** {@inheritDoc} */
    @GetMapping(value = { "/index"} )
    public final String indexHome(final Model model) {
        return "index_home";
    }
    @GetMapping(value = "/users")
    public String getAllUsers(Model model)
    {	
   
        List<User> users = userService.getList();
        //JSONObject jsonObject
        System.out.println("All User Data:::" + users);
        model.addAttribute("users", users);
        return "userList";
    }
    
    @GetMapping(value = "/users/{id}")
    public String getOneUser(@PathVariable(value="id") String id,Model model)
    {	
    	String Result ="";
    	try{
    		if( id != null && MemcachedUtils.memcachedGetData(id)!= null){    			
    			User userData =  MemcachedUtils.memcachedGetData(id);
    			Result ="Data is From Cache";
    			System.out.println("--------------------------------------------");
    			System.out.println("Data is From Cache !!");
    			System.out.println("--------------------------------------------");
    			System.out.println("Father ::: "+userData.getFatherName());
    			model.addAttribute("user", userData);
    			model.addAttribute("Result", Result);
    		}
    		else{
	    		User user = userService.findById(Long.parseLong(id)); 
	    		Result = MemcachedUtils.memcachedSetData(user,id);
	    		if(Result == null ){
	    			Result ="Memcached Connection Failure !!";
	    		}
	    		System.out.println("--------------------------------------------");
    			System.out.println("Data is From Database");
    			System.out.println("--------------------------------------------");
		        System.out.println("Result ::: "+ Result);	       
		        model.addAttribute("user", user);
		        model.addAttribute("Result", Result);
    		}
    	} catch (Exception e) {    		
    		System.out.println( e.getMessage() );
		}
        return "user";
    }
    
    /** {@inheritDoc} */
    @GetMapping(value = { "/user/{username}"} )
    public final String userUpdate(@PathVariable(value="username") String username,final Model model) {
    	User user = userService.findByUsername(username); 
    	System.out.println("User Data:::" + user);
    	model.addAttribute("user", user);
    	return "userUpdate";
    }
    @PostMapping(value = { "/user/{username}"} )
    public final String userUpdateProfile(@PathVariable(value="username") String username,final @ModelAttribute("user") User userForm,final Model model) {
    	User user = userService.findByUsername(username);
    	user.setUsername(userForm.getUsername());
    	user.setUserEmail(userForm.getUserEmail());
    	user.setDateOfBirth(userForm.getDateOfBirth());
    	user.setFatherName(userForm.getFatherName());
    	user.setMotherName(userForm.getMotherName());
    	user.setGender(userForm.getGender());
    	user.setLanguage(userForm.getLanguage());
    	user.setMaritalStatus(userForm.getMaritalStatus());
    	user.setNationality(userForm.getNationality());
    	user.setPermanentAddress(userForm.getPermanentAddress());
    	user.setTempAddress(userForm.getTempAddress());
    	user.setPhoneNumber(userForm.getPhoneNumber());
    	user.setSecondaryPhoneNumber(userForm.getSecondaryPhoneNumber());
    	user.setPrimaryOccupation(userForm.getPrimaryOccupation());
    	user.setSecondaryOccupation(userForm.getSecondaryOccupation());
    	user.setSkills(userForm.getSkills());
    	user.setWorkingExperience(userForm.getWorkingExperience());    	
    	userService.save(user); 
    	/*model.addAttribute("user", user);*/
    	return "welcome";
    }
    
    @RequestMapping(value={"/user/rabbit"}, method={RequestMethod.GET})
    public String rabbitmqSetUp() { 
    	System.out.println("Rabbit mq method is callled!!!");
      for (int i = 0; i < 20; i++) {
        producerService.produceMessage(generateString());
      }
      return "rabbitmq";
    }
    
    private static String generateString() {
      String uuid = UUID.randomUUID().toString();
      return "uuid = " + uuid;
    }
    

    
}
