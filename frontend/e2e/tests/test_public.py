"""E2E tests for public pages (home, login, booking form)."""
import os
import pytest

BASE = os.environ.get("E2E_BASE_URL", "http://airbnb-spa-frontend")


class TestHomePage:
    """SC-01 to SC-03: Home page and navigation."""

    def test_home_page_loads(self, page):
        """SC-01: Home page loads with hero section."""
        page.goto(BASE)
        page.wait_for_load_state("networkidle")
        assert page.locator("h1").is_visible()
        assert page.locator(".hero").is_visible()

    def test_navigation_links(self, page):
        """SC-02: Navigation links are present."""
        page.goto(BASE)
        page.wait_for_load_state("networkidle")
        nav = page.locator(".navbar-links")
        for link in ["Le Logement", "Spa", "Galerie", "Tarifs", "Disponibilités", "Réserver"]:
            assert nav.locator(f"text={link}").is_visible(), f"Link '{link}' not found"

    def test_connexion_button_for_anon(self, page):
        """SC-03: Unauthenticated user sees Connexion button."""
        page.goto(BASE)
        page.wait_for_load_state("networkidle")
        assert page.locator("text=Connexion").is_visible()


class TestLoginPage:
    """SC-04 to SC-07: Login functionality."""

    def test_login_form_displayed(self, page):
        """SC-04: Login form displays username and password fields."""
        page.goto(f"{BASE}/login")
        page.wait_for_load_state("networkidle")
        assert page.locator('input[formControlName="username"]').is_visible()
        assert page.locator('input[formControlName="password"]').is_visible()
        assert page.locator('button[type="submit"]').is_visible()

    def test_empty_form_shows_validation(self, login_page):
        """SC-05: Empty form shows validation errors."""
        login_page.navigate("/login")
        login_page.submit_button.click()
        assert login_page.page.locator(".form-error").is_visible()

    def test_wrong_credentials(self, login_page):
        """SC-06: Wrong credentials show error message."""
        login_page.navigate("/login")
        login_page.login("wrong", "wrong")
        assert login_page.error_alert.is_visible(timeout=5000)

    def test_successful_login(self, login_page):
        """SC-07: Successful login shows user menu."""
        login_page.navigate("/login")
        login_page.login("admin", "admin123")
        # Should redirect away from login page
        assert login_page.is_logged_in()

    def test_logout_redirects(self, page, login_page):
        """Verify logout works."""
        login_page.navigate("/login")
        login_page.login("admin", "admin123")
        assert login_page.is_logged_in()
        # Logout via dropdown
        page.locator(".navbar-user-btn").click()
        page.locator("text=Déconnexion").click()
        page.wait_for_load_state("networkidle")
        # Should see Connexion again
        assert page.locator("text=Connexion").is_visible()


class TestBookingForm:
    """SC-08 to SC-10: Booking form."""

    def test_booking_form_loads(self, page):
        """SC-08: Booking form loads with all fields."""
        page.goto(f"{BASE}/booking")
        page.wait_for_load_state("networkidle")
        fields = ["firstName", "lastName", "email", "phone", "startDate", "endDate", "numberOfGuests"]
        for field in fields:
            assert page.locator(f'[formControlName="{field}"]').is_visible(), f"Field '{field}' not found"

    def test_booking_form_validation(self, page):
        """SC-09: Validation errors for empty required fields."""
        page.goto(f"{BASE}/booking")
        page.wait_for_load_state("networkidle")
        page.locator('button[type="submit"]').click()
        # Should show validation errors
        error_count = page.locator(".form-error").count()
        assert error_count > 0, "No validation errors shown"