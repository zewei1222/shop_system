package com.example.demo;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.List;

@Component
public class myTerminal implements CommandLineRunner{
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    Scanner scanner = new Scanner(System.in);
    private User currentUser = null;

    @Override
    public void run(String... args) throws Exception {

        setupInitialAdmin();
        System.out.println("---Shop System Terminal Launch---");
        while(true){
            if(isLoggedIn()) {
                runMainMenu();
            }else{
                runLoginMenu();
            }
        }
    }

    public void setupInitialAdmin(){
        if (userService.isUserTableEmpty()) {
            System.out.println("---Create Admin User---");
            System.out.print("Adim username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            userService.createUser(username, password, Role.ROLE_ADMIN);
            System.out.println("Adim Created Successfully");
        }
    }

    public void runLoginMenu(){
        System.out.println("---Login Menu---");
        System.out.print("Username: ");
        String username =  scanner.nextLine();
        System.out.print("Password: ");
        String password =  scanner.nextLine();
        currentUser = userService.login(username, password);
    }

    public void runMainMenu(){
        System.out.println("---Main Menu---\n[A]dd [S]how [D]elet [U]pdate"
                +(currentUser.getRole()==Role.ROLE_ADMIN?" [C]reateStaffUser":"")+" [E]xit");

        String input = scanner.nextLine();
        switch (input.toUpperCase()){
            case "A":
                doAddProductToBuffer();
                break;
            case "S":
                doViewBuffer();
                break;
            case "D":
                productService.clearBuffer();
                break;
            case "U":
                doSaveBufferToDatabase();
                break;
            case "C":
                if(currentUser.getRole()==Role.ROLE_ADMIN){
                    doCreateStaffUser();
                }
                break;
            case "E":
                System.out.println("Logout.");
                currentUser = null;
                break;
            default:
                System.out.println("Invalid Input");
        }
    }

    public boolean isLoggedIn(){
        return currentUser != null;
    }

    public double safeDouble(String promo){
        while(true){
            try{
                System.out.print(promo);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid Input");
            }
        }
    }

    public int safeInteger(String promo){
        while(true){
            try{
                System.out.print(promo);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid Input");
            }
        }
    }

    private void doAddProductToBuffer() {
        System.out.println("---Add Product to Buffer---");
        System.out.print("name: ");
        String name =  scanner.nextLine();
        System.out.print("description: ");
        String description =  scanner.nextLine();
        double price =  safeDouble("price: ");
        int quantity =  safeInteger("quantity: ");
        System.out.print("category: ");
        String categoryName =  scanner.nextLine();
        Category category =  new Category(categoryName);
        productService.addProductToBuffer(new Product(name, description, price, quantity, category));
    }

    private void doViewBuffer() {
        System.out.println("---View Buffer---");
        List<Product> buffer = productService.getProductsFromBuffer();
        if (buffer.isEmpty()){
            System.out.println("There is no products in the buffer");
        }else{
            for (Product p: buffer) {
                System.out.println("Product name: " + p.getName()
                        + ", description: " + p.getDescription()
                        + ", price: " + p.getPrice()
                        + ", stock: " + p.getStock()
                        + ", category: " + p.getCategory().getName());
            }
        }
    }

    private void doSaveBufferToDatabase() {
        System.out.println("---Save Buffer to Database---");
        productService.saveBufferToDatabase();
    }

    private void doCreateStaffUser() {
        System.out.println("\n--- CreateStaffUser ---");
        System.out.print("username: ");
        String username = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();

        User newUser = userService.createUser(username, password, Role.ROLE_STAFF);
        if (newUser != null) {
            System.out.println("Staff '" + newUser.getUsername() + "' created successfully！");
        }
    }
}

