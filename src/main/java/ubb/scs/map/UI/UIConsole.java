//package ubb.scs.map.UI;
//
//import ubb.scs.map.domain.Friendship;
//import ubb.scs.map.domain.User;
//import ubb.scs.map.domain.exceptions.EntityAlreadyExistsException;
//import ubb.scs.map.domain.exceptions.EntityMissingException;
//import ubb.scs.map.domain.exceptions.UserAlreadyExistsException;
//import ubb.scs.map.domain.validators.ValidationException;
//import ubb.scs.map.service.FriendshipService;
//import ubb.scs.map.service.NetworkService;
//
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//import java.util.UUID;
//
//public class UIConsole {
//    private final NetworkService NetworkService;
//    private final FriendshipService friendshipService;
//    public UIConsole(NetworkService NetworkService, FriendshipService FriendshipService) {
//        this.NetworkService = NetworkService;
//        this.friendshipService = FriendshipService;
//    }
//
//    void printMenu(){
//        System.out.println();
//        System.out.println("0.Exit.");
//        System.out.println("1.Add user.");
//        System.out.println("2.Remove user.");
//        System.out.println("3.Update user.");
//        System.out.println("4.Add friendship.");
//        System.out.println("5.Remove friendship.");
//        System.out.println("6.Number of communities");
//        System.out.println("7.The  most friendly community");
//    }
//
//    private void addUser(){
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Username: ");
//        String username = scan.nextLine();
//        System.out.println("First name: ");
//        String firstName = scan.nextLine();
//        System.out.println("Last name: ");
//        String lastName = scan.nextLine();
//        System.out.println("Password: ");
//        String password = scan.nextLine();
//        try{
//            NetworkService.addUser(username, firstName, lastName, password);
//            System.out.println("User successfully added.");
//        }
//        catch(ValidationException | IllegalArgumentException | EntityAlreadyExistsException e){
//            System.out.println(e.getMessage());
//        }
//
//    }
//    private void deleteUser(){
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Username: ");
//        String username = scan.nextLine();
//        try{
//            NetworkService.deleteUser(username) ;
//            System.out.println("User successfully deleted.");
//        }
//        catch(EntityMissingException | IllegalArgumentException e){
//            System.out.println(e.getMessage());
//        }
//    }
//
//    private void updateUser(){
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Username: ");
//        String username = scan.nextLine();
//        System.out.println("First name: ");
//        String firstName = scan.nextLine();
//        System.out.println("Last name: ");
//        String lastName = scan.nextLine();
//        System.out.println("Password: ");
//        String password = scan.nextLine();
//        try{
//            NetworkService.updateUser(username, firstName, lastName, password);
//            System.out.println("User successfully updated.");
//        }catch (ValidationException | EntityMissingException e){
//            System.out.println(e.getMessage());
//        }
//    }
//    private void addFriendship(){
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Username of the first user: ");
//        String username1 = scan.nextLine();
//        System.out.println("Username of the second user: ");
//        String username2 = scan.nextLine();
//        try{
//            NetworkService.addFriendship(username1, username2);
//            System.out.println("Friendship successfully added.");
//        }
//        catch(IllegalArgumentException | EntityMissingException | EntityAlreadyExistsException | ValidationException e){
//            System.out.println(e.getMessage());
//        }
//
//    }
//
//    public void removeFriendship(){
//        Scanner scan = new Scanner(System.in);
//        System.out.println("Username of the first user: ");
//        String username1 = scan.nextLine();
//        System.out.println("Username of the second user: ");
//        String username2 = scan.nextLine();
//        try{
//            NetworkService.removeFriendship(username1, username2);
//            System.out.println("Friendship successfully removed.");
//        }
//        catch(IllegalArgumentException | EntityMissingException  e){
//            System.out.println(e.getMessage());
//        }
//
//    }
//
//    public void getTheNumberOfCommunities(){
//        System.out.println("Number of communities: ");
//        System.out.println(friendshipService.numberOfCommunities());
//
//    }
//
//    public void getTheMostFriendlyCommunity(){
//        System.out.println("The most friendly community:");
//        List<UUID> community = friendshipService.mostFriendlyCommunity();
//        for(UUID u : community){
//            System.out.println(u);
//        }
//    }
//
//    private void seeAllUsers(){
//        Iterable<User> users = NetworkService.getAllUsers();
//        for(User u : users){
//            System.out.println(u);
//        }
//
//    }
//    private void seeAllFriendships(){
//        Iterable<Friendship> friendships = NetworkService.getAllFriendships();
//        for(Friendship f : friendships){
//            System.out.println(f.getId().getE1() +" " +f.getId().getE2());
//        }
//    }
//
//
//    public void run(){
//
//        Scanner scan = new Scanner(System.in);
//        boolean exit = false;
//        while(!exit){
//            printMenu();
//            System.out.println("Your choice:");
//            int choice = scan.nextInt();
//            switch(choice){
//                case 0: exit = true; break;
//                case 1: addUser(); break;
//                case 2: deleteUser(); break;
//                case 3: updateUser(); break;
//                case 4: addFriendship(); break;
//                case 5: removeFriendship(); break;
//                case 6: getTheNumberOfCommunities(); break;
//                case 7: getTheMostFriendlyCommunity(); break;
//                case 8: seeAllUsers(); break;
//                case 9: seeAllFriendships(); break;
//                default:
//                    System.out.println("Invalid choice. Try again.");
//            }
//        }
//    }
//
//}
