package com.example.demo;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.List;

@Component
public class myTerminal implements CommandLineRunner {

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    private final Scanner scanner = new Scanner(System.in);
    private User currentUser = null;

    @Override
    public void run(String... args) {
        setupInitialAdmin();
        System.out.println("--- Shop System Terminal Launch ---");
        while (true) {
            if (isLoggedIn()) {
                runMainMenu();
            } else {
                runLoginMenu();
            }
        }
    }

    public void setupInitialAdmin() {
        if (userService.isUserTableEmpty()) {
            System.out.println("--- Setup Initial Admin ---");
            System.out.print("Enter Admin Username: ");
            String userName = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            User newUser = new User(null, userName, password, Role.ROLE_ADMIN);
            userService.addUser(newUser);
            System.out.println("Admin created successfully.");
        }
    }

    public void runLoginMenu() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        currentUser = userService.login(username, password);
        if (isLoggedIn()) {
            System.out.println("Login successful. Welcome " + currentUser.getName());
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    public void runMainMenu() {
        System.out.println("\n--- Main Menu ---");

        if (currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            System.out.println("[P]roduct  [C]ategory  [U]ser  [E]xit");
        } else {
            System.out.println("[P]roduct  [E]xit");
        }

        System.out.print("Select option: ");
        String input = scanner.nextLine().toUpperCase();

        switch (input) {
            case "P":
                runProductFunction();
                break;
            case "C":
                if (currentUser.getRole().equals(Role.ROLE_ADMIN)) {
                    runCategoryFunction();
                } else {
                    System.out.println("Permission denied.");
                }
                break;
            case "U":
                if (currentUser.getRole().equals(Role.ROLE_ADMIN)) {
                    runUserFunction();
                } else {
                    System.out.println("Permission denied.");
                }
                break;
            case "E":
                System.out.println("Logged out.");
                currentUser = null;
                break;
            default:
                System.out.println("Invalid input.");
        }
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public double safeDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }
    }

    public int safeInteger(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer format.");
            }
        }
    }

    public Long safeLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format.");
            }
        }
    }

    public void runProductFunction() {
        System.out.println("\n--- Product Menu ---");
        System.out.println("[C]reate  [S]earch  [D]elete  [B]ack");
        System.out.print("Select option: ");
        String input = scanner.nextLine().toUpperCase();

        switch (input) {
            case "C":
                doSaveProduct();
                break;
            case "S":
                doGetAllProducts();
                break;
            case "D":
                doDeleteProduct();
                break;
            case "B":
                break;
            default:
                System.out.println("Invalid input.");
        }
    }

    private Category selectCategory() {
        List<Category> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories available. Please contact Admin.");
            return null;
        }

        System.out.println("--- Select Category ---");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("[%d] %s%n", i + 1, categories.get(i).getName());
        }

        int index = safeInteger("Enter option number: ") - 1;
        if (index >= 0 && index < categories.size()) {
            return categories.get(index);
        } else {
            System.out.println("Invalid selection.");
            return null;
        }
    }

    private void doSaveProduct() {
        System.out.println("\n--- Add Product ---");

        Category selectedCategory = selectCategory();
        if (selectedCategory == null) return;

        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        double price = safeDouble("Price: ");
        int quantity = safeInteger("Stock Quantity: ");

        try {
            productService.createProduct(
                    new Product(null, name, description, price, quantity, selectedCategory, currentUser),
                    currentUser
            );
            System.out.println("Product saved successfully.");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void doGetAllProducts() {
        System.out.println("\n--- Product List ---");
        List<Product> productList = productService.getAllProducts(currentUser);
        if (productList.isEmpty()) {
            System.out.println("No products found.");
        } else {
            for (Product p : productList) {
                System.out.printf("ID: %d | Name: %s | Price: %.2f | Stock: %d | Category: %s%s%n",
                        p.getId(), p.getName(), p.getPrice(), p.getStock(), p.getCategory().getName(),
                        (currentUser.getRole() == Role.ROLE_ADMIN ? " | Owner: " + p.getOwner().getName() : "")
                );
            }
        }
    }

    private void doDeleteProduct() {
        System.out.println("\n--- Delete Product ---");
        doGetAllProducts();
        Long id = safeLong("Enter Product ID to delete: ");
        try {
            productService.deleteProduct(id, currentUser);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void runCategoryFunction() {
        System.out.println("\n--- Category Menu ---");
        System.out.println("[C]reate  [S]earch  [D]elete  [E]dit  [B]ack");
        System.out.print("Select option: ");

        String input = scanner.nextLine().toUpperCase();
        switch (input) {
            case "C":
                doAddCategory();
                break;
            case "S":
                doGetAllCategories();
                break;
            case "D":
                doDeleteCategory();
                break;
            case "E":
                doEditCategory();
                break;
            case "B":
                break;
            default:
                System.out.println("Invalid input.");
        }
    }

    private void doAddCategory() {
        System.out.println("\n--- Add Category ---");
        System.out.print("Category Name: ");
        String name = scanner.nextLine();
        try {
            categoryService.addCategory(new Category(null, name), currentUser);
            System.out.println("Category added successfully.");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void doGetAllCategories() {
        System.out.println("\n--- Category List ---");
        List<Category> categoryList = categoryService.getAllCategories();
        if (categoryList.isEmpty()) {
            System.out.println("No categories found.");
        }
        for (Category c : categoryList) {
            System.out.printf("ID: %d | Name: %s%n", c.getId(), c.getName());
        }
    }

    private void doDeleteCategory() {
        System.out.println("\n--- Delete Category ---");
        doGetAllCategories();
        Long id = safeLong("Enter Category ID to delete: ");
        try {
            categoryService.deleteCategory(id, currentUser);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void doEditCategory() {
        System.out.println("\n--- Edit Category ---");
        doGetAllCategories();
        Long id = safeLong("Enter Category ID to edit: ");
        System.out.print("Enter New Name: ");
        String newName = scanner.nextLine();
        try {
            categoryService.updateCategory(id, newName, currentUser);
            System.out.println("Category updated successfully.");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void runUserFunction() {
        System.out.println("\n--- User Menu ---");
        System.out.println("[C]reate  [S]earch  [D]elete  [B]ack");
        System.out.print("Select option: ");
        String input = scanner.nextLine().toUpperCase();
        switch (input) {
            case "C":
                doAddUser();
                break;
            case "S":
                doGetAllUsers();
                break;
            case "D":
                doDeleteUser();
                break;
            case "B":
                break;
            default:
                System.out.println("Invalid input.");
        }
    }

    private void doAddUser() {
        System.out.println("\n--- Create User ---");
        System.out.print("Username: ");
        String userName = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Select Role: [A]dmin / [U]ser");
        System.out.print("Choice: ");
        String roleInput = scanner.nextLine().toUpperCase();
        Role role;

        if (roleInput.equals("A")) {
            role = Role.ROLE_ADMIN;
        } else if (roleInput.equals("U")) {
            role = Role.ROLE_USER; // Or ROLE_STAFF depending on your Enum
        } else {
            System.out.println("Invalid role selection.");
            return;
        }
        User newUser = new User(null, userName, password, role);
        userService.addUser(newUser);
        try {
            userService.addUser(newUser);
            System.out.println("User created successfully.");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void doGetAllUsers() {
        System.out.println("\n--- User List ---");
        List<User> userList = userService.getAllUsers(currentUser);
        if (userList.isEmpty()) {
            System.out.println("No users found.");
        }
        for (User user : userList) {
            System.out.printf("ID: %d | Username: %s | Role: %s%n", user.getId(), user.getName(), user.getRole());
        }
    }

    private void doDeleteUser() {
        System.out.println("\n--- Delete User ---");
        System.out.print("Enter Username to delete: ");
        String userName = scanner.nextLine();
        try {
            userService.deleteUser(userName, currentUser);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}