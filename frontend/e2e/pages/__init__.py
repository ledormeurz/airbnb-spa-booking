"""Page object models for Le Nid Spa booking app e2e tests."""
import os
from datetime import datetime, timedelta
from typing import Optional


class BasePage:
    """Base page with common locators and helpers."""

    BASE_URL = os.environ.get("E2E_BASE_URL", "http://airbnb-spa-frontend")

    def __init__(self, page):
        self.page = page

    def navigate(self, path: str = ""):
        self.page.goto(f"{self.BASE_URL}{path}")
        self.page.wait_for_load_state("networkidle")

    def wait_for_spinner_to_disappear(self):
        """Wait for loading spinner to disappear."""
        try:
            self.page.wait_for_selector(".spinner", state="hidden", timeout=5000)
        except Exception:
            pass

    def get_alert_message(self) -> Optional[str]:
        """Get text from an alert-success or alert-error element."""
        alert = self.page.locator(".alert-success, .alert-error").first
        if alert.is_visible():
            return alert.text_content()
        return None

    def screenshot(self, name: str):
        """Take a screenshot for debugging."""
        self.page.screenshot(path=f"/tmp/e2e_{name}.png")


class LoginPage(BasePage):
    """Login page (/login) page object."""

    @property
    def username_input(self):
        return self.page.locator('input[formControlName="username"]')

    @property
    def password_input(self):
        return self.page.locator('input[formControlName="password"]')

    @property
    def submit_button(self):
        return self.page.locator('button[type="submit"]')

    @property
    def error_alert(self):
        return self.page.locator(".alert-error")

    def login(self, username: str, password: str):
        """Fill credentials and submit login form."""
        self.username_input.fill(username)
        self.password_input.fill(password)
        self.submit_button.click()
        self.page.wait_for_load_state("networkidle")

    def is_logged_in(self) -> bool:
        """Check if user menu appears after login."""
        return self.page.locator(".navbar-user-btn").is_visible(timeout=3000)


class ProfilePage(BasePage):
    """User profile page (/user/profile) page object."""

    def navigate(self):
        super().navigate("/user/profile")
        self.wait_for_spinner_to_disappear()

    @property
    def username_display(self):
        return self.page.locator(".profile-value").first

    @property
    def edit_button(self):
        return self.page.locator('text=Modifier')

    @property
    def save_button(self):
        return self.page.locator('button:has-text("Enregistrer")')

    @property
    def first_name_input(self):
        return self.page.locator('input[formControlName="firstName"]')

    @property
    def last_name_input(self):
        return self.page.locator('input[formControlName="lastName"]')

    @property
    def success_alert(self):
        return self.page.locator(".alert-success")

    @property
    def spa_card(self):
        return self.page.locator(".spa-card")

    @property
    def role_badge(self):
        return self.page.locator(".profile-info .badge")


class MyBookingsPage(BasePage):
    """My bookings page (/user/bookings) page object."""

    def navigate(self):
        super().navigate("/user/bookings")
        self.wait_for_spinner_to_disappear()

    @property
    def empty_state(self):
        return self.page.locator(".empty-state")

    @property
    def booking_cards(self):
        return self.page.locator(".booking-card")

    def get_booking_card(self, index: int = 0):
        return self.booking_cards.nth(index)

    @property
    def cancel_buttons(self):
        return self.page.locator(".booking-card .btn-danger")

    def click_cancel(self, index: int = 0):
        self.cancel_buttons.nth(index).click()


class BookingDetailPage(BasePage):
    """Booking detail page (/user/bookings/:id) page object."""

    @property
    def detail_card(self):
        return self.page.locator(".detail-card")

    @property
    def back_button(self):
        return self.page.locator('text=Retour')

    @property
    def cancel_button(self):
        return self.page.locator('text=Annuler la réservation')

    @property
    def edit_button(self):
        return self.page.locator('text=Modifier')


class DashboardPage(BasePage):
    """Admin dashboard page (/admin/dashboard) page object."""

    def navigate(self):
        super().navigate("/admin/dashboard")
        self.wait_for_spinner_to_disappear()

    @property
    def stat_cards(self):
        return self.page.locator(".stat-card")

    @property
    def stat_values(self):
        return self.page.locator(".stat-value")

    @property
    def quick_links(self):
        return self.page.locator(".quick-link-card")

    @property
    def upcoming_section(self):
        return self.page.locator(".upcoming-section")


class AdminBookingsPage(BasePage):
    """Admin bookings page (/admin/bookings) page object."""

    def navigate(self):
        super().navigate("/admin/bookings")
        self.wait_for_spinner_to_disappear()

    @property
    def filter_selects(self):
        return self.page.locator(".filters-bar select")

    @property
    def status_filter(self):
        return self.page.locator(".filters-bar select").first

    @property
    def date_inputs(self):
        return self.page.locator('.filters-bar input[type="date"]')

    @property
    def table(self):
        return self.page.locator(".table")

    @property
    def table_rows(self):
        return self.page.locator(".table tbody tr")

    @property
    def confirm_buttons(self):
        return self.page.locator('.btn-success:has-text("Confirmer")')

    @property
    def reject_buttons(self):
        return self.page.locator('.btn-danger:has-text("Refuser")')


class AdminCalendarPage(BasePage):
    """Admin calendar page (/admin/calendar) page object."""

    def navigate(self):
        super().navigate("/admin/calendar")
        self.wait_for_spinner_to_disappear()

    @property
    def calendar_grid(self):
        return self.page.locator(".calendar-grid")

    @property
    def prev_button(self):
        return self.page.locator('.calendar-nav button').first

    @property
    def next_button(self):
        return self.page.locator('.calendar-nav button').last

    @property
    def month_title(self):
        return self.page.locator(".calendar-header h3")

    @property
    def block_start_date(self):
        return self.page.locator("#blockStart")

    @property
    def block_end_date(self):
        return self.page.locator("#blockEnd")

    @property
    def block_reason(self):
        return self.page.locator("#blockReason")

    @property
    def block_button(self):
        return self.page.locator('button:has-text("Bloquer")')

    @property
    def blocks_list(self):
        return self.page.locator(".blocks-list")

    @property
    def delete_block_buttons(self):
        return self.page.locator(".block-item .btn-danger")


class AdminPricesPage(BasePage):
    """Admin prices page (/admin/prices) page object."""

    def navigate(self):
        super().navigate("/admin/prices")
        self.wait_for_spinner_to_disappear()

    @property
    def table_rows(self):
        return self.page.locator(".table tbody tr")

    @property
    def edit_buttons(self):
        return self.page.locator('button:has-text("Modifier")')

    @property
    def save_buttons(self):
        return self.page.locator('button:has-text("Sauvegarder")')

    @property
    def cancel_edit_buttons(self):
        return self.page.locator('button:has-text("Annuler")')

    @property
    def price_inputs(self):
        return self.page.locator(".price-input")

    @property
    def toggle_switches(self):
        return self.page.locator(".toggle-switch input[type='checkbox']")


class AdminEquipmentPage(BasePage):
    """Admin equipment page (/admin/equipment) page object."""

    def navigate(self):
        super().navigate("/admin/equipment")
        self.wait_for_spinner_to_disappear()

    @property
    def add_button(self):
        return self.page.locator('button:has-text("Ajouter")')

    @property
    def equipment_cards(self):
        return self.page.locator(".equipment-card")

    @property
    def modal(self):
        return self.page.locator(".modal")

    @property
    def modal_name(self):
        return self.page.locator("#eqName")

    @property
    def modal_description(self):
        return self.page.locator("#eqDesc")

    @property
    def modal_icon(self):
        return self.page.locator("#eqIcon")

    @property
    def modal_save(self):
        return self.page.locator(".modal-footer .btn-primary")

    @property
    def modal_close(self):
        return self.page.locator(".modal-close")

    @property
    def edit_buttons(self):
        return self.page.locator('button:has-text("Modifier")')

    @property
    def delete_buttons(self):
        return self.page.locator('.equipment-card .btn-danger')


class AdminUsersPage(BasePage):
    """Admin users page (/admin/users) page object."""

    def navigate(self):
        super().navigate("/admin/users")
        self.wait_for_spinner_to_disappear()

    @property
    def create_button(self):
        return self.page.locator('button:has-text("Créer")')

    @property
    def table_rows(self):
        return self.page.locator(".table tbody tr")

    @property
    def modal(self):
        return self.page.locator(".modal")

    @property
    def modal_username(self):
        return self.page.locator("#uUsername")

    @property
    def modal_first_name(self):
        return self.page.locator("#uFirstName")

    @property
    def modal_last_name(self):
        return self.page.locator("#uLastName")

    @property
    def modal_email(self):
        return self.page.locator("#uEmail")

    @property
    def modal_password(self):
        return self.page.locator("#uPassword")

    @property
    def modal_role(self):
        return self.page.locator("#uRole")

    @property
    def modal_save(self):
        return self.page.locator(".modal-footer .btn-primary")

    @property
    def edit_buttons(self):
        return self.page.locator('button:has-text("Modifier")')