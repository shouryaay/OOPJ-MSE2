# OOPJ (CS336B) – MSE-2 Practical

**Shourya Kesarwani** · IT · Roll No. 202501100600160 · Section C · Evening shift

| Folder | Question | Tech |
|---|---|---|
| [`Q1-OrderManagement`](Q1-OrderManagement) | Order Management System (menu-driven) | Java JDBC + MySQL (`ecommerce_db`) |
| [`Q2-LibraryManagement`](Q2-LibraryManagement) | Library Management REST API | Spring Boot + JdbcTemplate + MySQL (`library_db`) |

Both are Maven projects. Import in Eclipse: *File → Import → Maven → Existing Maven Projects*.
MySQL runs on `localhost:3306` (user `root`); both databases are created automatically on first run.

## Q1 – Order Management System (JDBC)
Run `com.ecommerce.OrderManagementApp` → *Run As → Java Application* and use the menu:

| Option | Part | What it does |
|---|---|---|
| 1 / 2 | (a) | Insert sample orders / a new order with `PreparedStatement`; duplicate `order_id` is detected and skipped |
| 4 | (b) | Orders with the given status and `quantity > 2`, sorted by `unit_price DESC` |
| 5 | (c) | Increase the quantity of an order; message if the order does not exist |
| 6 | (d) | Update status only if the order exists and the status is valid (Pending / Shipped / Delivered / Cancelled), then show the updated order |
| 7 | (e) | Total value `SUM(quantity * unit_price)`; delete an order only if its status is `Cancelled` |

## Q2 – Library Management REST API (Spring Boot)
Run `com.college.library.LibraryManagementApplication` → *Run As → Java Application*. Base URL `http://localhost:8080/books`.

| Method | URL | Part | Responses |
|---|---|---|---|
| POST | `/books` | (b) | 201 Created · 400 invalid data · 409 duplicate id |
| GET | `/books` | (c) | 200 OK (list of all books) |
| GET | `/books/{id}` | (c) | 200 OK · 404 Not Found |
| PUT | `/books/{id}` | (d) | 200 OK (updated book) · 400 · 404 |
| DELETE | `/books/{id}` | (e) | 200 OK · 404 Not Found |

A ready-made Postman collection is in `Q2-LibraryManagement/Library-API.postman_collection.json`.
