package org.example;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public interface Data {

    /*
     * Item contains product with the amount
     */
    class Item {
        private Product product = null;
        private final int amount;
        private final double item_cost;

        public Item(Product product, int amount) {
            this.product = product;
            this.amount = amount;
            // TODO control the item_cost amount of decimal places to 2
            this.item_cost = (product.product_price * amount);
        }

        public Product getProduct() {
            return product;
        }

        public int getAmount() {
            return amount;
        }

        public double getCost() {
            return item_cost;
        }
    }

    class Product {
        final int product_id;
        final String product_name;
        final double product_price;
        final double discount;
        final int supplier_id;
        final boolean isAvailable;

        public Product(int productId, String product_name, double product_price, double discount, int supplierId, boolean isAvailable) {
            this.product_id = productId;
            this.product_name = product_name;
            this.product_price = product_price;
            this.discount = discount;
            this.supplier_id = supplierId;
            this.isAvailable = isAvailable;
        }

        @Override
        public String toString() {
            // product price to 2 decimal places
            DecimalFormat df = new DecimalFormat("£0.00");
            return product_name + " - " + df.format(product_price) + " | " + (isAvailable ? "Available" : "Out of Stock"); // using £ symbol instead of $ for price
        }
    }

    class User {
        private UUID user_id;
        private String user_name;
        private String short_id;
        private int house_id;
        private int order_id;

        public User(String user_id, String user_name, String short_id, int house_id) {
            if (user_id == null) {
                this.user_id = null;
            } else {
                this.user_id = UUID.fromString(user_id);
            }

            this.user_name = user_name;
            this.short_id = short_id;
            this.house_id = house_id;
        }

        public UUID getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            if (user_id == null) {
                this.user_id = null;
            } else {
                this.user_id = UUID.fromString(user_id);
            }
        }

        public String getUser_name() {
            return this.user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public String getShort_id() {
            return short_id;
        }

        public void setShort_id(String short_id) {
            this.short_id = short_id;
        }

        public int getHouse_id() {
            return house_id;
        }

        public void setHouse_id(int house_id) {
            this.house_id = house_id;
        }

        public int getOrder_id() {
            return order_id;
        }

        @Override
        public String toString() {
            return "User ID: " + user_id + " | Name: " + user_name + " | Short ID: " + short_id + " | House ID: " + house_id;
        }
    }

    class Group {
        private int group_id;
        private int house_id;
        private String order_status;
        private double order_cost;
        private String group_name;
        private UUID owner_id;

        private final ArrayList<Order> orderList = new ArrayList<>();

        public Group(int house_id, String group_name, UUID owner_id) {
            this.house_id = house_id;
            this.group_name = group_name;
            this.owner_id = owner_id;
        }

        public boolean addSubOrder(Order Order) {
            if (Order == null) {
                return false;
            }

            orderList.add(Order);
            return true;
        }

        public UUID getOwner_id() {
            return owner_id;
        }

        public void setOrder_house_id(int house_id) {
            this.house_id = house_id;
        }

        public int getOrder_house_id() {
            return house_id;
        }

        public void setOrder_cost(float order_cost) {
            this.order_cost = order_cost;
        }

        public double getOrder_cost() {
            return order_cost;
        }

        public void calculateOrderCost() {
            float totalCost = 0;
            for (Order Order : orderList) {
                totalCost += Order.sub_order_cost;
            }
            this.order_cost = totalCost;
        }

        public String getOrder_status() {
            return order_status;
        }

        public void setOrder_status(String order_status) {
            this.order_status = order_status;
        }

        public int getGroup_id() {
            return group_id;
        }

        public String getGroup_name() {
            return group_name;
        }

        public void setGroup_id(int group_id) {
            this.group_id = group_id;
        }
    }

    class Order {
        private int sub_order_id; // id is automatically generated by postgres
        private final User user;
        private final ArrayList<Item> itemsList = new ArrayList<>();
        private double sub_order_cost;
        private int order_id;

        public Order(User user) {
            this.user = user;
            calculateSub_order_cost();
        }

        public int setSub_order_id(int sub_order_id) {
            this.sub_order_id = sub_order_id;
            return sub_order_id;
        }

        public int getSub_order_id() {
            return sub_order_id;
        }

        public boolean addItem(Product product, int amount) {
            // return false if the product is not available
            if (!product.isAvailable) {
                return false;
            }

            if (amount <= 0) {
                return false;
            }

            Item newItem = new Item(product, amount);
            itemsList.add(newItem);
            return true;
        }

        public void calculateSub_order_cost() {
            double totalCost = 0;
            for (Item item : itemsList) {
                totalCost += item.item_cost;
            }
            this.sub_order_cost = totalCost;
        }

        public double getSub_order_cost() {
            return sub_order_cost;
        }

        public int getOrder_id() {
            return order_id;
        }

        public void setOrder_id(int order_id) {
            this.order_id = order_id;
        }
    }
}
