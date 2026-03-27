// init 時塞東西進去 mongo db

//package com.transaction;
//
//import com.transaction.domain.entity.User;
//import com.transaction.domain.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.Random;
//
//@Component
//public class MongoTestRunner implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//
//    // To track which Pod is doing the work in K8s
//    @Value("${app.pod.name:local-dev}")
//    private String podName;
//
//    public MongoTestRunner(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("========== Starting Mongo Sharding Test from Pod: " + podName + " ==========");
//
//        // 1. Clean up (Optional: Only if you want a fresh start)
//        // userRepository.deleteAll();
//
//        // 2. Prepare 20 test users
//        List<User> testUsers = new ArrayList<>();
//        Random random = new Random();
//
//        for (int i = 1; i <= 50; i++) {
//            User user = new User();
//            // Important: Use "userid" as the field for sharding
//            user.setUserId("User_" + System.currentTimeMillis() % 10);
//            user.setMoney((long) random.nextInt(10000));
//            user.setLevel(random.nextInt(10) + 1);
//            testUsers.add(user);
//        }
//
//        // 3. Bulk Insert into Mongos
//        userRepository.saveAll(testUsers);
//
//        System.out.println("Successfully inserted 20 users into sharded cluster!");
//        System.out.println("========================================================================");
//    }
//}
