package com.coopmanager.config;

import com.coopmanager.entity.*;
import com.coopmanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ProductionEntryRepository productionRepo;
    private final FinancialTransactionRepository transactionRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        log.info("Seeding initial data...");

        // Create President
        Member president = memberRepository.save(Member.builder()
                .firstName("Mohammed")
                .lastName("Ouziad")
                .email("president@coopmanager.ma")
                .password(passwordEncoder.encode("President@123"))
                .phone("0661234567")
                .cultureType("Olive & Argan")
                .joinDate(LocalDate.of(2020, 1, 15))
                .role(Role.PRESIDENT)
                .active(true)
                .build());

        // Create Treasurer
        Member treasurer = memberRepository.save(Member.builder()
                .firstName("Fatima")
                .lastName("Bensouda")
                .email("tresorier@coopmanager.ma")
                .password(passwordEncoder.encode("Tresorier@123"))
                .phone("0662345678")
                .cultureType("Argan")
                .joinDate(LocalDate.of(2020, 3, 10))
                .role(Role.TREASURER)
                .active(true)
                .build());

        // Create Members
        Member member1 = memberRepository.save(Member.builder()
                .firstName("Ahmed")
                .lastName("Kaddouri")
                .email("ahmed@coopmanager.ma")
                .password(passwordEncoder.encode("Membre@123"))
                .phone("0663456789")
                .cultureType("Olive")
                .joinDate(LocalDate.of(2020, 5, 20))
                .role(Role.MEMBER)
                .active(true)
                .build());

        Member member2 = memberRepository.save(Member.builder()
                .firstName("Aicha")
                .lastName("Tahiri")
                .email("aicha@coopmanager.ma")
                .password(passwordEncoder.encode("Membre@123"))
                .phone("0664567890")
                .cultureType("Argan")
                .joinDate(LocalDate.of(2021, 2, 1))
                .role(Role.MEMBER)
                .active(true)
                .build());

        Member member3 = memberRepository.save(Member.builder()
                .firstName("Hassan")
                .lastName("Mribet")
                .email("hassan@coopmanager.ma")
                .password(passwordEncoder.encode("Membre@123"))
                .cultureType("Olive")
                .joinDate(LocalDate.of(2021, 6, 15))
                .role(Role.MEMBER)
                .active(true)
                .build());

        // Seed production entries
        ProductionEntry pe1 = productionRepo.save(ProductionEntry.builder()
                .member(member1).productType("Olive").quantityKg(new BigDecimal("350.5"))
                .harvestDate(LocalDate.now().minusDays(10))
                .status(ProductionEntry.ValidationStatus.VALIDATED)
                .validatedBy(treasurer).validatedAt(java.time.LocalDateTime.now().minusDays(8))
                .build());

        productionRepo.save(ProductionEntry.builder()
                .member(member2).productType("Argan").quantityKg(new BigDecimal("220.0"))
                .harvestDate(LocalDate.now().minusDays(8))
                .status(ProductionEntry.ValidationStatus.VALIDATED)
                .validatedBy(treasurer).validatedAt(java.time.LocalDateTime.now().minusDays(6))
                .build());

        productionRepo.save(ProductionEntry.builder()
                .member(member3).productType("Olive").quantityKg(new BigDecimal("175.25"))
                .harvestDate(LocalDate.now().minusDays(5))
                .status(ProductionEntry.ValidationStatus.PENDING)
                .build());

        // Seed financial transactions
        transactionRepo.save(FinancialTransaction.builder()
                .type(FinancialTransaction.TransactionType.INCOME)
                .category(FinancialTransaction.TransactionCategory.SALE)
                .description("Vente huile d'olive - lot 001")
                .amount(new BigDecimal("45000"))
                .transactionDate(LocalDate.now().minusDays(15))
                .reference("VTE-2025-001")
                .recordedBy(treasurer)
                .build());

        transactionRepo.save(FinancialTransaction.builder()
                .type(FinancialTransaction.TransactionType.EXPENSE)
                .category(FinancialTransaction.TransactionCategory.LOGISTICS)
                .description("Transport vers Casablanca")
                .amount(new BigDecimal("2500"))
                .transactionDate(LocalDate.now().minusDays(14))
                .reference("DEP-2025-001")
                .recordedBy(treasurer)
                .build());

        transactionRepo.save(FinancialTransaction.builder()
                .type(FinancialTransaction.TransactionType.EXPENSE)
                .category(FinancialTransaction.TransactionCategory.INPUTS)
                .description("Engrais et produits phytosanitaires")
                .amount(new BigDecimal("8000"))
                .transactionDate(LocalDate.now().minusDays(30))
                .reference("DEP-2025-002")
                .recordedBy(treasurer)
                .build());

        log.info("✓ Seeded {} members, production entries and transactions.", memberRepository.count());
        log.info("  President: president@coopmanager.ma / President@123");
        log.info("  Treasurer: tresorier@coopmanager.ma / Tresorier@123");
        log.info("  Member:    ahmed@coopmanager.ma / Membre@123");
    }
}
