/*
 * =========================
 * Change 4 pull request :)
 * =========================
 */
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * =========================
 * ENUM: Тип операции
 * =========================
 */
enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}

/*
 * =========================
 * КЛАСС Customer (Клиент)
 * =========================
 */
class Customer {
    private static int counter = 1;
    private int id;
    private String fullName;

    public Customer(String fullName) {
        this.id = counter++;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}

/*
 * =========================
 * АБСТРАКТНЫЙ КЛАСС Account
 * =========================
 */
abstract class Account {
    private static int counter = 1000;
    private String accountNumber;
    private double balance;
    private Customer owner;

    public Account(Customer owner) {
        this.owner = owner;
        this.accountNumber = "ACC" + (++counter);
        this.balance = 0;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }

    public boolean transfer(Account to, double amount) {
        if (to == null || amount <= 0) {
            return false;
        }
        if (!withdraw(amount)) {
            return false;
        }
        to.deposit(amount);
        return true;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    protected void setBalance(double value) {
        this.balance = value;
    }

    public Customer getOwner() {
        return owner;
    }

    public abstract String getType();
}

/*
 * =========================
 * DebitAccount
 * =========================
 */
class DebitAccount extends Account {
    public DebitAccount(Customer owner) {
        super(owner);
    }

    @Override
    public String getType() {
        return "Дебетовый";
    }
}

/*
 * =========================
 * CreditAccount
 * =========================
 */
class CreditAccount extends Account {
    private double creditLimit;

    public CreditAccount(Customer owner, double creditLimit) {
        super(owner);
        this.creditLimit = creditLimit;
    }

    @Override
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }
        if (getBalance() - amount < -creditLimit) {
            return false;
        }
        setBalance(getBalance() - amount);
        return true;
    }

    @Override
    public String getType() {
        return "Кредитный (лимит: " + creditLimit + ")";
    }
}

/*
 * =========================
 * Transaction
 * =========================
 */
class Transaction {
    private TransactionType type;
    private double amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime timestamp;
    private boolean success;
    private String message;

    public Transaction(TransactionType type,
                       double amount,
                       String fromAccountNumber,
                       String toAccountNumber,
                       boolean success,
                       String message) {
        this.type = type;
        this.amount = amount;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String timeStr = timestamp.format(formatter);
        return String.format("%-19s | %-10s | %10.2f | От: %-15s | К: %-15s | %-8s | %s",
                timeStr,
                type,
                amount,
                fromAccountNumber != null ? fromAccountNumber : "---",
                toAccountNumber != null ? toAccountNumber : "---",
                success ? "УСПЕХ" : "ОШИБКА",
                message);
    }
}

/*
 * =========================
 * Bank
 * =========================
 */
class Bank {
    private ArrayList<Customer> customers = new ArrayList<>();
    private ArrayList<Account> accounts = new ArrayList<>();
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public Customer createCustomer(String fullName) {
        Customer c = new Customer(fullName);
        customers.add(c);
        System.out.println("\n✅ Создан клиент:");
        System.out.println("   ID: " + c.getId());
        System.out.println("   ФИО: " + c.getFullName());
        return c;
    }

    public Account openDebitAccount(Customer owner) {
        if (owner == null) {
            System.out.println("❌ Ошибка: клиент не найден");
            return null;
        }
        Account acc = new DebitAccount(owner);
        accounts.add(acc);
        System.out.println("\n✅ Открыт дебетовый счёт:");
        System.out.println("   Номер счёта: " + acc.getAccountNumber());
        System.out.println("   Владелец: " + owner.getFullName() + " (ID: " + owner.getId() + ")");
        return acc;
    }

    public Account openCreditAccount(Customer owner, double creditLimit) {
        if (owner == null) {
            System.out.println("❌ Ошибка: клиент не найден");
            return null;
        }
        if (creditLimit <= 0) {
            System.out.println("❌ Ошибка: кредитный лимит должен быть больше 0");
            return null;
        }
        Account acc = new CreditAccount(owner, creditLimit);
        accounts.add(acc);
        System.out.println("\n✅ Открыт кредитный счёт:");
        System.out.println("   Номер счёта: " + acc.getAccountNumber());
        System.out.println("   Владелец: " + owner.getFullName() + " (ID: " + owner.getId() + ")");
        System.out.println("   Кредитный лимит: " + creditLimit);
        return acc;
    }

    public Account findAccount(String number) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(number)) {
                return acc;
            }
        }
        return null;
    }

    public boolean deposit(String accNum, double amount) {
        Account acc = findAccount(accNum);
        boolean success;
        String message;
        
        if (acc == null) {
            success = false;
            message = "Счёт не найден";
        } else if (amount <= 0) {
            success = false;
            message = "Сумма должна быть больше 0";
        } else {
            success = acc.deposit(amount);
            message = success ? "Пополнение успешно" : "Ошибка пополнения";
        }
        
        transactions.add(new Transaction(
                TransactionType.DEPOSIT,
                amount,
                null,
                accNum,
                success,
                message
        ));
        
        if (success) {
            System.out.println("\n✅ Счёт " + accNum + " пополнен на " + amount);
            System.out.println("   Новый баланс: " + acc.getBalance());
        } else {
            System.out.println("\n❌ Ошибка пополнения: " + message);
        }
        
        return success;
    }

    public boolean withdraw(String accNum, double amount) {
        Account acc = findAccount(accNum);
        boolean success;
        String message;
        
        if (acc == null) {
            success = false;
            message = "Счёт не найден";
        } else if (amount <= 0) {
            success = false;
            message = "Сумма должна быть больше 0";
        } else {
            success = acc.withdraw(amount);
            message = success ? "Снятие успешно" : "Недостаточно средств";
        }
        
        transactions.add(new Transaction(
                TransactionType.WITHDRAW,
                amount,
                accNum,
                null,
                success,
                message
        ));
        
        if (success) {
            System.out.println("\n✅ Со счёта " + accNum + " снято " + amount);
            System.out.println("   Новый баланс: " + acc.getBalance());
        } else {
            System.out.println("\n❌ Ошибка снятия: " + message);
        }
        
        return success;
    }

    public boolean transfer(String from, String to, double amount) {
        Account accFrom = findAccount(from);
        Account accTo = findAccount(to);
        boolean success;
        String message;
        
        if (accFrom == null) {
            success = false;
            message = "Счёт отправителя не найден";
        } else if (accTo == null) {
            success = false;
            message = "Счёт получателя не найден";
        } else if (amount <= 0) {
            success = false;
            message = "Сумма должна быть больше 0";
        } else if (from.equals(to)) {
            success = false;
            message = "Нельзя переводить на тот же счёт";
        } else {
            success = accFrom.transfer(accTo, amount);
            message = success ? "Перевод выполнен" : "Недостаточно средств для перевода";
        }
        
        transactions.add(new Transaction(
                TransactionType.TRANSFER,
                amount,
                from,
                to,
                success,
                message
        ));
        
        if (success) {
            System.out.println("\n✅ Перевод выполнен:");
            System.out.println("   От: " + from);
            System.out.println("   К: " + to);
            System.out.println("   Сумма: " + amount);
            System.out.println("   Баланс отправителя: " + accFrom.getBalance());
            System.out.println("   Баланс получателя: " + accTo.getBalance());
        } else {
            System.out.println("\n❌ Ошибка перевода: " + message);
        }
        
        return success;
    }

    public void printCustomerAccounts(int customerId) {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("СЧЕТА КЛИЕНТА ID: " + customerId);
        System.out.println("══════════════════════════════════════════");
        
        boolean foundCustomer = false;
        boolean foundAccounts = false;
        
        // Найти клиента
        Customer customer = null;
        for (Customer c : customers) {
            if (c.getId() == customerId) {
                customer = c;
                foundCustomer = true;
                System.out.println("Клиент: " + c.getFullName());
                break;
            }
        }
        
        if (!foundCustomer) {
            System.out.println("❌ Клиент с ID " + customerId + " не найден");
            System.out.println("══════════════════════════════════════════\n");
            return;
        }
        
        // Найти счета клиента
        for (Account acc : accounts) {
            if (acc.getOwner().getId() == customerId) {
                foundAccounts = true;
                System.out.printf("   Номер: %-15s | Тип: %-25s | Баланс: %10.2f%n",
                        acc.getAccountNumber(),
                        acc.getType(),
                        acc.getBalance());
            }
        }
        
        if (!foundAccounts) {
            System.out.println("   У клиента нет открытых счетов");
        }
        
        System.out.println("══════════════════════════════════════════\n");
    }

    public void printTransactions() {
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("ИСТОРИЯ ТРАНЗАКЦИЙ");
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        
        if (transactions.isEmpty()) {
            System.out.println("Транзакций нет");
        } else {
            System.out.println("Дата/Время          | Тип       |     Сумма | От                | К                  | Статус   | Сообщение");
            System.out.println("─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
            for (Transaction t : transactions) {
                System.out.println(t);
            }
        }
        
        System.out.println("════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════\n");
    }

    public void printReport() {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("ОТЧЁТ БАНКА");
        System.out.println("══════════════════════════════════════════");
        
        // Статистика по счетам
        int debitCount = 0;
        int creditCount = 0;
        double debitSum = 0;
        double creditSum = 0;
        
        for (Account acc : accounts) {
            if (acc instanceof DebitAccount) {
                debitCount++;
                debitSum += acc.getBalance();
            } else {
                creditCount++;
                creditSum += acc.getBalance();
            }
        }
        
        System.out.println("📊 СТАТИСТИКА ПО СЧЕТАМ:");
        System.out.println("   Дебетовые счета:");
        System.out.println("      Количество: " + debitCount);
        System.out.println("      Суммарный баланс: " + String.format("%.2f", debitSum));
        System.out.println("   Кредитные счета:");
        System.out.println("      Количество: " + creditCount);
        System.out.println("      Суммарный баланс: " + String.format("%.2f", creditSum));
        System.out.println("   Всего счетов: " + (debitCount + creditCount));
        System.out.println("   Общий баланс: " + String.format("%.2f", (debitSum + creditSum)));
        
        // Статистика по операциям
        int successOps = 0;
        int failedOps = 0;
        
        for (Transaction t : transactions) {
            if (t.isSuccess()) {
                successOps++;
            } else {
                failedOps++;
            }
        }
        
        System.out.println("\n📈 СТАТИСТИКА ПО ОПЕРАЦИЯМ:");
        System.out.println("   Успешных операций: " + successOps);
        System.out.println("   Неуспешных операций: " + failedOps);
        System.out.println("   Всего операций: " + (successOps + failedOps));
        
        // Количество клиентов
        System.out.println("\n👥 КЛИЕНТЫ:");
        System.out.println("   Количество клиентов: " + customers.size());
        
        System.out.println("══════════════════════════════════════════\n");
    }

    public Customer getCustomerById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public void printAllCustomers() {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("СПИСОК КЛИЕНТОВ");
        System.out.println("══════════════════════════════════════════");
        
        if (customers.isEmpty()) {
            System.out.println("Клиентов нет");
        } else {
            for (Customer c : customers) {
                System.out.println("   ID: " + c.getId() + " | ФИО: " + c.getFullName());
            }
        }
        
        System.out.println("══════════════════════════════════════════\n");
    }
}

/*
 * =========================
 * Main
 * =========================
 */
public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        Scanner sc = new Scanner(System.in);
        
        System.out.println("══════════════════════════════════════════");
        System.out.println("    БАНКОВСКАЯ СИСТЕМА v2.0");
        System.out.println("══════════════════════════════════════════");

        while (true) {
            printMenu();
            System.out.print("➤ Выберите действие (1-10): ");
            
            try {
                int cmd = sc.nextInt();
                
                switch (cmd) {
                    case 1 -> {
                        sc.nextLine(); // Очистка буфера
                        System.out.print("Введите ФИО клиента: ");
                        String fullName = sc.nextLine();
                        if (!fullName.trim().isEmpty()) {
                            bank.createCustomer(fullName);
                        } else {
                            System.out.println("❌ Ошибка: ФИО не может быть пустым");
                        }
                    }
                    
                    case 2 -> {
                        bank.printAllCustomers();
                        System.out.print("Введите ID клиента: ");
                        int debitId = sc.nextInt();
                        Customer debitCustomer = bank.getCustomerById(debitId);
                        if (debitCustomer != null) {
                            bank.openDebitAccount(debitCustomer);
                        } else {
                            System.out.println("❌ Ошибка: клиент с ID " + debitId + " не найден");
                        }
                    }
                    
                    case 3 -> {
                        bank.printAllCustomers();
                        System.out.print("Введите ID клиента: ");
                        int creditId = sc.nextInt();
                        System.out.print("Введите кредитный лимит: ");
                        double limit = sc.nextDouble();
                        Customer creditCustomer = bank.getCustomerById(creditId);
                        if (creditCustomer != null) {
                            bank.openCreditAccount(creditCustomer, limit);
                        } else {
                            System.out.println("❌ Ошибка: клиент с ID " + creditId + " не найден");
                        }
                    }
                    
                    case 4 -> {
                        System.out.print("Введите номер счёта для пополнения: ");
                        String depositAcc = sc.next();
                        System.out.print("Введите сумму пополнения: ");
                        double depositAmount = sc.nextDouble();
                        bank.deposit(depositAcc, depositAmount);
                    }
                    
                    case 5 -> {
                        System.out.print("Введите номер счёта для снятия: ");
                        String withdrawAcc = sc.next();
                        System.out.print("Введите сумму снятия: ");
                        double withdrawAmount = sc.nextDouble();
                        bank.withdraw(withdrawAcc, withdrawAmount);
                    }
                    
                    case 6 -> {
                        System.out.print("Введите номер счёта отправителя: ");
                        String fromAcc = sc.next();
                        System.out.print("Введите номер счёта получателя: ");
                        String toAcc = sc.next();
                        System.out.print("Введите сумму перевода: ");
                        double transferAmount = sc.nextDouble();
                        bank.transfer(fromAcc, toAcc, transferAmount);
                    }
                    
                    case 7 -> {
                        bank.printAllCustomers();
                        System.out.print("Введите ID клиента: ");
                        int clientId = sc.nextInt();
                        bank.printCustomerAccounts(clientId);
                    }
                    
                    case 8 -> {
                        bank.printTransactions();
                    }
                    
                    case 9 -> {
                        bank.printReport();
                    }
                    
                    case 10 -> {
                        System.out.println("\n══════════════════════════════════════════");
                        System.out.println("Спасибо за использование банковской системы!");
                        System.out.println("══════════════════════════════════════════");
                        sc.close();
                        System.exit(0);
                    }
                    
                    default -> {
                        System.out.println("❌ Ошибка: введите число от 1 до 10");
                    }
                }
                
                // Пауза для удобства чтения
                if (cmd != 10) {
                    System.out.println("\n══════════════════════════════════════════");
                    System.out.print("Нажмите Enter для продолжения...");
                    sc.nextLine(); // Очистка предыдущего ввода
                    sc.nextLine(); // Ожидание Enter
                }
                
            } catch (Exception e) {
                System.out.println("❌ Ошибка ввода: " + e.getMessage());
                sc.nextLine(); // Очистка неверного ввода
            }
        }
    }
    
    private static void printMenu() {
        System.out.println("\n══════════════════════════════════════════");
        System.out.println("           ГЛАВНОЕ МЕНЮ");
        System.out.println("══════════════════════════════════════════");
        System.out.println("1  ➤ Создать клиента");
        System.out.println("2  ➤ Открыть дебетовый счёт");
        System.out.println("3  ➤ Открыть кредитный счёт");
        System.out.println("4  ➤ Пополнить счёт");
        System.out.println("5  ➤ Снять со счёта");
        System.out.println("6  ➤ Перевести между счетами");
        System.out.println("7  ➤ Показать счета клиента");
        System.out.println("8  ➤ Показать все транзакции");
        System.out.println("9  ➤ Отчёт банка");
        System.out.println("10 ➤ Выход");
        System.out.println("══════════════════════════════════════════");
    }
}
