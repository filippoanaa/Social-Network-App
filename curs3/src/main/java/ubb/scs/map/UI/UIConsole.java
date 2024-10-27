package ubb.scs.map.UI;

import ubb.scs.map.domain.User;
import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
import ubb.scs.map.domain.exceptions.EntityMissingException;
import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
import ubb.scs.map.domain.validators.ValidationException;
import ubb.scs.map.service.Service;
import ubb.scs.map.service.Service;

import java.util.List;
import java.util.Scanner;

public class UIConsole {
    private final Service Service;
    public UIConsole(Service Service) {
        this.Service = Service;
    }

    void printMenu(){
        System.out.println("0.Exit.");
        System.out.println("1.Add user.");
        System.out.println("2.Remove user.");
        System.out.println("3.Add friendship.");
        System.out.println("4.Remove friendship.");
        System.out.println("5.Number of communities");
        System.out.println("6.The  most friendly community");
    }

    private void addUser(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Username: ");
        String username = scan.nextLine();
        System.out.println("First name: ");
        String firstName = scan.nextLine();
        System.out.println("Last name: ");
        String lastName = scan.nextLine();
        try{
            Service.addUser(username, firstName, lastName);
            System.out.println("User successfully added.");
        }
        catch(ValidationException | IllegalArgumentException | UserAlreadyExistsException e){
            System.out.println(e.getMessage());
        }

    }
    private void deleteUser(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Username: ");
        String username = scan.nextLine();
        try{
            Service.deleteUser(username) ;
        }
        catch(EntityMissingException | IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }

    private void addFriendship(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Username of the first user: ");
        String username1 = scan.nextLine();
        System.out.println("Username of the second user: ");
        String username2 = scan.nextLine();
        try{
            Service.addFriendship(username1, username2);
            System.out.println("Friendship successfully added.");
        }
        catch(IllegalArgumentException | EntityMissingException | EntityAlreadyExistsException | ValidationException e){
            System.out.println(e.getMessage());
        }

    }

    public void removeFriendship(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Username of the first user: ");
        String username1 = scan.nextLine();
        System.out.println("Username of the second user: ");
        String username2 = scan.nextLine();
        try{
            Service.removeFriendship(username1, username2);
            System.out.println("Friendship successfully removed.");
        }
        catch(IllegalArgumentException | EntityMissingException  e){
            System.out.println(e.getMessage());
        }

    }

    public void getTheNumberOfCommunities(){
        System.out.println("Number of communities: ");
        System.out.println(Service.numberOfCommunities());

    }

    public void getTheMostFriendlyCommunity(){
        System.out.println("The most friendly community:");
        List<User> community = Service.getTheMostFriendlyCommunity();
        for(User u : community){
            System.out.println(u);
        }
    }


    public void run(){

        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        while(!exit){
            Service.refreshFriends();
            printMenu();
            System.out.println("Your choice:");
            int choice = scan.nextInt();
            switch(choice){
                case 0: exit = true; break;
                case 1: addUser(); break;
                case 2: deleteUser(); break;
                case 3: addFriendship(); break;
                case 4: removeFriendship(); break;
                case 5: getTheNumberOfCommunities(); break;
                case 6: getTheMostFriendlyCommunity(); break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

}
