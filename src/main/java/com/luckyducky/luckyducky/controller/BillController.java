package com.luckyducky.luckyducky.controller;

import com.luckyducky.luckyducky.model.*;
import com.luckyducky.luckyducky.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
public class BillController {
    private final UserRepository userRepo;
    private final TransactionRepository transRepo;
    private final BillRepository billRepo;
    private final CategoryRepository cateRepo;
    private final BudgetRepository budgetRepo;

    public BillController(UserRepository userRepo, TransactionRepository transRepo, BillRepository billRepo, CategoryRepository cateRepo, BudgetRepository budgetRepo) {
        this.userRepo = userRepo;
        this.billRepo = billRepo;
        this.transRepo = transRepo;
        this.cateRepo = cateRepo;
        this.budgetRepo = budgetRepo;
    }

/////////////////  Show Bills  //////////////////////////
    @GetMapping("/bills")
    public String showBill(Model model) {
        // Creates empty Bill to send to HTML
        Bill bill = new Bill();
        // Get current user
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Get List of bills by user
        List<Bill> myBills = billRepo.findBillsByUser(user);
        // Send model with list of user bills, all categories, and a empty bill for the add modal to HTML
        model.addAttribute("bills", myBills);
        model.addAttribute("categories", cateRepo.findAll());
        model.addAttribute("newBill", bill);
        // Go to HTML
        return "bills/index";
    }

/////////////////  Add Bills  ///////////////////////////
    @PostMapping("/bills/add")
    // Using the object from the bills/index HTML in the add Modal
    public String newBill(@ModelAttribute Bill bill) {
        // Get the current user and their list of Transactions
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Transaction> transactions = new ArrayList<>();
        // Set the bill isPaid, User and Transaction properties
        bill.setPaid(false);
        bill.setUser(user);
        bill.setTransactions(transactions);
        // Save the new Bill to the database
        billRepo.save(bill);
        // Go back to the index of Bills by the URL so that the new info loads
        return "redirect:/bills";
    }

/////////////////  Edit Bills  //////////////////////////
    @PostMapping("/bills/edit")
    // Use the params from the bills/index HTML in the edit modal
    public String editBill(@RequestParam long id, @RequestParam String name, @RequestParam String date, @RequestParam int amount){
        // Parse the date string to a LocalDate type
        LocalDate lDate = LocalDate.parse(date);
        // using Id get the current Bill with that Id
        Bill bill = billRepo.getOne(id);
        // Get the current User
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Create a new bill and use all the information above to create it
        Bill updatedBill = new Bill(id,name,amount,lDate,bill.isPaid(),bill.getCreatedAt(),user,bill.getTransactions());
        // Save and override the bill in the database based on the Id
        billRepo.save(updatedBill);
        // Go back to the index of Bills by the URL so that the new info loads
        return "redirect:/bills";
    }


/////////////////  Delete Bills  ////////////////////////
    @PostMapping("/bills/delete")
    // Use the Id param from the bills/index HTML in the delete modal
    public String deleteBill(@RequestParam String id) {
        // Parse the Id to a long so it matches the Id in the Bill Model
        long realId = Long.parseLong(id);
        // Use the parsed Id to get the corresponding Bill from the database
        Bill bill = billRepo.getOne(realId);
        // Delete the Bill from the database
        billRepo.delete(bill);
        // Go back to the index of Bills by the URL so that the new info loads
        return "redirect:/bills";
    }

/////////////////  Pay Bill  /////////////////////////
    @PostMapping("/bills/payment")
    // Use the params from the bills/index HTML in the payBill modal
    public String payBill(@RequestParam int payAmt, @RequestParam String payName, @RequestParam long id) {
        // Create a empty Transaction
        Transaction payment = new Transaction();
        // Get the Category object for Bills
        Category cat = cateRepo.getOne(1L);
        // Get the current user and their budget
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Budget budget = budgetRepo.findBudgetByUserAndName(user, "main");
        // Set the empty Transaction with all the data
        payment.setName(payName);
        payment.setAmountInCents(payAmt);
        payment.setIncome(false);
        payment.setCategory(cat);
        // Add the Transaction to the current user's budget and save to the DB
        budget.getTransactions().add(payment);
        budgetRepo.save(budget);
        // Get bill that was paid from database and change paid status to true
        Bill bill = billRepo.getOne(id);
        bill.setPaid(true);
        bill.setAmountInCents(payAmt);
        billRepo.save(bill);
        // Go back to the index of Bills by the URL so that the new info loads
        return "redirect:/bills";
    }

}