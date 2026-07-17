# E2E Test Scenarios — Le Nid Spa Booking

## 1. Public Pages

### 1.1 Home Page (/)
- SC-01: Loads hero section, featured sections, CTA buttons
- SC-02: Navigation links work (Property, Spa, Galerie, Tarifs, Disponibilités, Réserver)
- SC-03: Unauthenticated user sees "Connexion" button in navbar

### 1.2 Login Page (/login)
- SC-04: Login form displays username and password fields
- SC-05: Empty form shows validation errors
- SC-06: Wrong credentials show error message
- SC-07: Successful login redirects and shows user menu

### 1.3 Booking Form (/booking)
- SC-08: Form loads with all fields (firstName, lastName, email, phone, dates, guests, type, message, agreedToRules)
- SC-09: Validation errors display for empty required fields
- SC-10: Successful booking shows confirmation

## 2. Authenticated User Pages

### 2.1 Profile (/user/profile)
- SC-11: Displays current user info (username, firstName, lastName, email, role badge)
- SC-12: Premium spa card is visible
- SC-13: Edit mode: fields pre-filled, save updates profile
- SC-14: Success message appears after saving

### 2.2 My Bookings (/user/bookings)
- SC-15: Shows loading spinner then list of bookings
- SC-16: Empty state shows when no bookings exist
- SC-17: Booking card shows dates, status badge, booking type, price
- SC-18: PENDING bookings show cancel button
- SC-19: Cancel triggers confirmation dialog
- SC-20: Clicking card navigates to booking detail

### 2.3 Booking Detail (/user/bookings/:id)
- SC-21: Shows booking details (client info, dates, status, price, message)
- SC-22: Back button navigates to booking list
- SC-23: PENDING booking shows edit and cancel buttons

## 3. Admin Pages (require ADMIN role)

### 3.1 Dashboard (/admin/dashboard)
- SC-24: Four stat cards display (en attente, confirmées, revenu, occupation)
- SC-25: Quick links navigate to admin sections
- SC-26: Upcoming arrivals/departures sections visible

### 3.2 Admin Bookings (/admin/bookings)
- SC-27: Filter bar loads with status, type, date range, client search
- SC-28: Table displays all bookings with client, dates, type, status, price
- SC-29: Confirmer button works for PENDING bookings
- SC-30: Refuser button prompts for reason
- SC-31: Filters narrow results

### 3.3 Admin Calendar (/admin/calendar)
- SC-32: Month grid renders with day headers
- SC-33: Prev/next month navigation works
- SC-34: Block form creates new block with start/end dates + reason
- SC-35: Existing blocks display in sidebar with delete button
- SC-36: Delete block triggers confirmation

### 3.4 Admin Prices (/admin/prices)
- SC-37: Table shows all price rules with name, type, dayType, price, active
- SC-38: Edit icon shows price input + save button
- SC-39: Active toggle saves state
- SC-40: Success message on price update

### 3.5 Admin Equipment (/admin/equipment)
- SC-41: Grid displays all equipment with icon, name, description, active badge
- SC-42: "Ajouter" button opens modal with form fields
- SC-43: Edit opens pre-filled modal
- SC-44: Save creates/updates equipment
- SC-45: Delete triggers confirmation

### 3.6 Admin Users (/admin/users)
- SC-46: Table displays all users with username, firstName, lastName, email, role, enabled
- SC-47: "Créer" button opens modal with all fields incl. password
- SC-48: Edit opens modal WITHOUT password field
- SC-49: Role and enabled toggles work on edit
- SC-50: Password hash NEVER displayed
