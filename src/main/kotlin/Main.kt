import kotlin.concurrent.thread
import java.io.File
import java.io.FileReader
import java.io.FileNotFoundException
import java.io.FileWriter

// class for food
class Food(val name: String, var price: Int, var minutes: Int, var count: Int) {
    // Companion object to provide utility functions for Food class
    companion object {
        // Parse a Food object from a CSV string
        fun fromCsv(csvString: String): Food {
            val strings: List<String> = csvString.split(' ', ',').toList()

            val name = strings[0]
            val price = strings[1].toInt()
            val minutes = strings[2].toInt()
            val count = strings[3].toInt()

            return Food(name, price, minutes, count)
        }
    }

    // Function to set the state of the Food object
    fun setState(state: FoodState) {
        price = state.price
        minutes = state.minutes
        count = state.count
    }

    // Override toString method to provide a string representation of the Food object
    override fun toString(): String {
        return "${name} - ${price}, duration: ${minutes}"
    }

}

interface IOrder {
    // Function to add a food item to the order
    fun addFood(food: Food)

    // Function to remove a food item from the order
    fun removeFood(food: Food)

    // Function to start processing the order
    fun order()

    // Function to check if the order is ready
    fun isReady(): Boolean

    // Function to cancel the order
    fun cancel()

    // Property of the total cost of all dishes in the order
    val totalCost: Int
}

class Order(private val client: IClient) : IOrder {
    private val items: MutableList<Food> = mutableListOf()
    private var _isReady: Boolean = false
    private var _isCancelled: Boolean = false

    override fun addFood(food: Food) {
        items.add(food)
    }

    override fun removeFood(food: Food) {
        items.remove(food)
    }

    override fun order() {
        println("Order processing started for items: $items")

        val maxTime = items.maxOf { it.minutes }

        // Start a new thread to monitor order cancellation
        thread {
            var currentTime = 0
            while (currentTime < maxTime) {
                if (_isCancelled) {
                    println("Order cancelled.")
                    return@thread
                }
                Thread.sleep(1000) // Sleep for 1 second
                currentTime++
            }
            // If the loop completes without cancellation, set the order as ready
            _isReady = true
            client.receiveOrder(this)
        }
    }

    override fun isReady(): Boolean {
        return _isReady
    }

    override fun cancel() {
        _isCancelled = true
    }

    override val totalCost
        get() = items.sumOf { it.price }

    override fun toString(): String {
        return "Order: " + items.joinToString(",\n")
    }
}


// Data class to represent the state of a food item
data class FoodState(val price: Int, val minutes: Int, val count: Int)

interface IClient {
    val server: IServer

    // Function to create an order with a specific food item
    fun createOrder(food: Food): Unit

    // Function to add a food item to the current order
    fun addFood(food: Food): Unit

    // Function to remove a food item from the current order
    fun removeFood(food: Food): Unit

    // Function to send the order to the server
    fun order(): IOrder

    // Function to receive and process an order from the server
    fun receiveOrder(order: IOrder): Unit
}


interface IFoodDao {
    fun addFood(food: Food)
    fun removeFood(food: Food)
    fun findFoodByName(name: String): Food?
    fun getFoods(): List<Food>
    fun updateFood(foodName: String, state: FoodState)
}

object FoodDao : IFoodDao {
    private val foodList: MutableList<Food> = mutableListOf()

    override fun addFood(food: Food) {
        if (foodList.any { it.name == food.name }) {
            throw IllegalArgumentException("Food '${food.name}' already exists in the menu.")
        } else {
            foodList.add(food)
        }
    }

    override fun removeFood(food: Food) {
        if (!foodList.remove(food)) {
            throw RuntimeException("Food '${food.name}' does not exist in the menu.")
        }
    }

    override fun findFoodByName(name: String): Food? {
        return foodList.find { it.name == name }
    }

    override fun getFoods(): List<Food> {
        return foodList.toList()
    }

    override fun updateFood(foodName: String, state: FoodState) {
        val foodToUpdate = foodList.find { it.name == foodName }
        foodToUpdate?.setState(state)
    }
}

interface IIncomeDao {
    fun getIncome(): Int
    fun addIncome(sum: Int)
}

object IncomeDao : IIncomeDao {
    private var _income = 0

    override fun addIncome(sum: Int) {
        if (sum > 0) {
            _income += sum
        }
    }

    override fun getIncome(): Int {
        return _income
    }
}

interface IServer {
    fun registerOrder(order: IOrder)
    fun cancelOrder(order: IOrder)
    fun giveOutOrder(order: IOrder)
    fun getFoods(): List<Food>
    fun removeFood(food: Food)
    fun addFoodToDataBase(food: Food)
    fun changeFoodInformation(food: Food, state: FoodState)
}

class Server(private val foodDao: IFoodDao, private val incomeDao: IIncomeDao) : IServer {
    override fun registerOrder(order: IOrder) {
        println("Order registered: $order")
    }

    override fun cancelOrder(order: IOrder) {
        order.cancel()
    }

    override fun giveOutOrder(order: IOrder) {
        val orderCost = order.totalCost
        incomeDao.addIncome(orderCost)

        val totalCost = incomeDao.getIncome()
        println("Total cost: $totalCost")
    }

    override fun getFoods(): List<Food> {
        return foodDao.getFoods()
    }

    override fun removeFood(food: Food) {
        foodDao.removeFood(food)
    }

    override fun addFoodToDataBase(food: Food) {
        foodDao.addFood(food)
    }

    override fun changeFoodInformation(food: Food, state: FoodState) {
        foodDao.updateFood(food.name, state)
    }
}


class Client(override val server: IServer) : IClient {
    private var currentOrder: Order = Order(this)

    // Function to create an order with a specific food item
    override fun createOrder(food: Food) {
        currentOrder.addFood(food)
    }

    // Function to add a food item to the current order
    override fun addFood(food: Food) {
        currentOrder.addFood(food)
    }

    // Function to remove a food item from the current order
    override fun removeFood(food: Food) {
        currentOrder.removeFood(food)
    }

    // Function to send the order to the server
    override fun order(): IOrder {
        server.registerOrder(currentOrder)
        return currentOrder
    }

    // Function to receive and process an order from the server
    override fun receiveOrder(order: IOrder) {
        // Implementation for receiving and processing an order from the server
        server.giveOutOrder(order)
        currentOrder = Order(this)
    }
}

interface IWaiter {
    fun addFoodToMenu(food: Food)
    fun removeFoodFromMenu(food: Food)
    fun changeFoodInMenu(food: Food, state: FoodState)
    fun displayMenu()
}

class Waiter(private val server: IServer) : IWaiter {
    override fun addFoodToMenu(food: Food) {
        server.addFoodToDataBase(food)
    }

    override fun removeFoodFromMenu(food: Food) {
        server.removeFood(food)
    }

    override fun changeFoodInMenu(food: Food, state: FoodState) {
        server.changeFoodInformation(food, state)
    }

    override fun displayMenu() {
        val foods = server.getFoods()
        println("Menu:")
        for (food in foods) {
            println("- $food")
        }
    }
}

class InputHandler(private val menuManager: MenuManager, private val orderProcessor: OrderProcessor) {
    fun handleInput(input: String) {
        val inputArgs = input.split(" ")
        when (inputArgs[0]) {
            "waiter" -> {
                // Handle waiter actions
                when (inputArgs[1]) {
                    "add" -> {
                        if (inputArgs[2] == "food") {

                            menuManager.addFoodToMenu(inputArgs.subList(3, inputArgs.size).joinToString(" "))
                        }
                    }

                    "remove" -> {
                        if (inputArgs[2] == "food") {
                            menuManager.removeFoodFromMenu(inputArgs.subList(3, inputArgs.size).joinToString(" "))
                        }
                    }

                    "food" -> {
                        menuManager.modifyFood(inputArgs)
                    }
                }
            }

            "display" -> {
                if (inputArgs[1] == "menu") {
                    menuManager.displayMenu()
                }
            }

            "user" -> {
                // Handle user actions
                when (inputArgs[1]) {
                    "order" -> {
                        orderProcessor.addToOrder(inputArgs.subList(3, inputArgs.size).joinToString(" "))
                    }

                    "remove" -> {
                        orderProcessor.removeFromOrder(inputArgs.subList(3, inputArgs.size).joinToString(" "))
                    }

                    "cancel" -> {
                        orderProcessor.cancelOrder()
                    }

                    "get" -> {
                        orderProcessor.getOrderStatus()
                    }
                }
            }
        }
    }
}

class MenuManager(private val server: IServer) {
    fun addFoodToMenu(foodName: String) {
        if (foodName.isNotBlank()) { // Check if foodName is not blank
            val food = Food(foodName, 0, 0, 1)
            server.addFoodToDataBase(food)
            println("$foodName added to the menu.")
        } else {
            println("Invalid food name. Please provide a non-empty name.")
        }
    }

    fun removeFoodFromMenu(foodName: String) {
        val food = FoodDao.findFoodByName(foodName)
        if (food != null) {
            server.removeFood(food)
            println("$foodName removed from the menu.")
        } else {
            println("Food not found in the menu.")
        }
    }


    fun modifyFood(inputArgs: List<String>) {
        if (inputArgs.size > 7) {
            println("Invalid command. Please provide complete information.")
            return
        }

        val foodName = inputArgs[2]
        val food = FoodDao.findFoodByName(foodName)

        if (food == null) {
            println("Food not found in the menu.")
            return
        }

        val _property = inputArgs[4]
        val value = inputArgs[5]

        if (value?.toIntOrNull() == null || value.toInt() <= 0) {
            println("Invalid property.")
        } else {

            when (_property) {

                "number" -> {
                    food.count = value.toInt()
                    println("Number of $foodName set to $value.")
                }

                "price" -> {
                    food.price = value.toInt()
                    println("Price of $foodName set to $value.")
                }

                "duration" -> {
                    food.minutes = value.toInt()
                    println("Duration of $foodName set to $value minutes.")
                }

                else -> {
                    println("Invalid property.")
                }
            }
        }
    }


    fun displayMenu() {

        val foods = server.getFoods()
        println("Menu:")
        for (food in foods) {
            println("- $food")
        }
    }
}

class OrderProcessor(private val client: IClient) {
    fun addToOrder(foodName: String) {
        val food = FoodDao.findFoodByName(foodName)
        if (food != null) {
            client.addFood(food)
            println("$foodName added to the order.")
        } else {
            println("Food not found in the menu.")
        }
    }

    fun removeFromOrder(foodName: String) {
        val food = FoodDao.findFoodByName(foodName)
        if (food != null) {
            client.removeFood(food)
            println("$foodName removed from the order.")
        } else {
            println("Food not found in the menu.")
        }
    }

    fun cancelOrder() {
        val order = client.order()
        order.cancel()
        println("Order cancelled.")
    }

    fun getOrderStatus() {
        val order = client.order()
        if (order.isReady()) {
            println("Order received and paid.")
        } else {
            println("Order is not ready yet.")
        }
    }
}


class AppRunner(private val inputHandler: InputHandler) {
    fun run() {
        println("App is running!")

        while (true) {
            displayOptionsMenu()
        }
    }

    private fun displayOptionsMenu() {
        println("Input: 'exit', to exit the program.")
        println("Input: 'waiter add food <nameOfFood>' to add a new food to the menu.")
        println("Input: 'waiter remove food <nameOfFood>' to remove the food from the menu.")
        println("Input: 'display menu' to display the menu on the console.")
        println("Input: 'waiter food <nameOfFood> set number <number>', to set the number for the special food in the menu.")
        println("Input: 'waiter food <nameOfFood> set price <price>', to set price for the special food in the menu.")
        println("Input: 'waiter food <nameOfFood> set duration <duration>', to set the duration for the special food in the menu.")
        println("Input: 'user order food <nameOfFood>' to add a food to the current order.")
        println("Input: 'user remove food <nameOfFood>' to remove food from the current order.")
        println("Input: 'user cancel order' to cancel the current order.")
        println("Input: 'user get order' to get the order and pay for the order (if the order is ready).")

        val line = readLine() ?: return
        inputHandler.handleInput(line)
    }
}


fun main() {
    val server: IServer = Server(FoodDao, IncomeDao)
    val client: IClient = Client(server)
    val inputHandler = InputHandler(MenuManager(server), OrderProcessor(client))
    val appRunner = AppRunner(inputHandler)
    appRunner.run()
}
