"""Shared fixtures for e2e tests."""
import pytest
from playwright.sync_api import sync_playwright, Browser, Page, Playwright

from pages import (
    LoginPage, ProfilePage, MyBookingsPage, BookingDetailPage,
    DashboardPage, AdminBookingsPage, AdminCalendarPage,
    AdminPricesPage, AdminEquipmentPage, AdminUsersPage
)


@pytest.fixture(scope="session")
def playwright_instance():
    """Create a single Playwright instance for the session."""
    with sync_playwright() as p:
        yield p


@pytest.fixture(scope="session")
def browser(playwright_instance: Playwright) -> Browser:
    """Create browser instance (once per session)."""
    browser = playwright_instance.chromium.launch(
        headless=True,
        args=["--no-sandbox", "--disable-setuid-sandbox"]
    )
    yield browser
    browser.close()


@pytest.fixture
def page(browser: Browser) -> Page:
    """Create a new browser context and page for each test."""
    context = browser.new_context(
        viewport={"width": 1280, "height": 800},
        ignore_https_errors=True,
    )
    p = context.new_page()
    p.set_default_timeout(10000)
    yield p
    context.close()


# ==================== Helper Fixtures ====================

@pytest.fixture
def login_page(page: Page) -> LoginPage:
    return LoginPage(page)


@pytest.fixture
def profile_page(page: Page) -> ProfilePage:
    return ProfilePage(page)


@pytest.fixture
def my_bookings_page(page: Page) -> MyBookingsPage:
    return MyBookingsPage(page)


@pytest.fixture
def booking_detail_page(page: Page) -> BookingDetailPage:
    return BookingDetailPage(page)


@pytest.fixture
def dashboard_page(page: Page) -> DashboardPage:
    return DashboardPage(page)


@pytest.fixture
def admin_bookings_page(page: Page) -> AdminBookingsPage:
    return AdminBookingsPage(page)


@pytest.fixture
def admin_calendar_page(page: Page) -> AdminCalendarPage:
    return AdminCalendarPage(page)


@pytest.fixture
def admin_prices_page(page: Page) -> AdminPricesPage:
    return AdminPricesPage(page)


@pytest.fixture
def admin_equipment_page(page: Page) -> AdminEquipmentPage:
    return AdminEquipmentPage(page)


@pytest.fixture
def admin_users_page(page: Page) -> AdminUsersPage:
    return AdminUsersPage(page)


@pytest.fixture
def admin_logged_in(page: Page, login_page: LoginPage) -> str:
    """Log in as admin and return username."""
    login_page.navigate("/login")
    login_page.login("admin", "admin123")
    assert login_page.is_logged_in(), "Admin login failed"
    return "admin"


@pytest.fixture
def user_logged_in(page: Page, login_page: LoginPage) -> str:
    """Log in as a regular user and return username."""
    login_page.navigate("/login")
    login_page.login("user", "user123")
    assert login_page.is_logged_in(), "User login failed"
    return "user"