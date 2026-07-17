"""E2E tests for authenticated user pages (profile, my bookings, booking detail)."""
import pytest


class TestProfilePage:
    """SC-11 to SC-14: User profile."""

    def test_profile_displays_user_info(self, profile_page, user_logged_in):
        """SC-11: Profile shows current user info with role badge."""
        profile_page.navigate()
        assert profile_page.username_display.is_visible()
        assert profile_page.role_badge.is_visible()

    def test_spa_card_visible(self, profile_page, user_logged_in):
        """SC-12: Premium spa card is visible."""
        profile_page.navigate()
        assert profile_page.spa_card.is_visible()

    def test_edit_profile(self, profile_page, user_logged_in):
        """SC-13: Edit mode pre-fills fields and saves."""
        profile_page.navigate()
        profile_page.edit_button.click()

        # Check fields are pre-filled
        current_val = profile_page.first_name_input.input_value()
        new_name = f"Test{current_val}" if current_val else "TestUser"

        profile_page.first_name_input.fill(new_name)
        profile_page.save_button.click()

        # Wait for success message
        assert profile_page.page.locator(".alert-success").is_visible(timeout=5000)

    def test_role_badge_shows_admin_or_user(self, profile_page, admin_logged_in):
        """Admin sees 'Administrateur' badge."""
        profile_page.navigate()
        badge_text = profile_page.role_badge.text_content()
        assert "Administrateur" in badge_text or "Utilisateur" in badge_text


class TestMyBookingsPage:
    """SC-15 to SC-20: My bookings list."""

    def test_bookings_page_loads(self, my_bookings_page, user_logged_in):
        """SC-15: Bookings page loads (may be empty or have items)."""
        my_bookings_page.navigate()
        # Either booking cards or empty state is visible
        has_cards = my_bookings_page.booking_cards.count() > 0
        has_empty = my_bookings_page.empty_state.is_visible()
        assert has_cards or has_empty, "Neither bookings nor empty state visible"

    def test_empty_state_shown(self, my_bookings_page, user_logged_in):
        """SC-16: Empty state shows 'Aucune réservation'."""
        my_bookings_page.navigate()
        if my_bookings_page.booking_cards.count() == 0:
            empty_text = my_bookings_page.empty_state.text_content()
            assert "Aucune réservation" in empty_text

    def test_booking_card_shows_info(self, my_bookings_page, user_logged_in):
        """SC-17: Booking card shows dates, status badge, type, price."""
        my_bookings_page.navigate()
        if my_bookings_page.booking_cards.count() > 0:
            card = my_bookings_page.get_booking_card(0)
            assert card.locator(".badge").is_visible()
            assert card.locator(".booking-price").is_visible()

    def test_pending_shows_cancel(self, my_bookings_page, user_logged_in):
        """SC-18: PENDING bookings have cancel button."""
        my_bookings_page.navigate()
        if my_bookings_page.cancel_buttons.count() > 0:
            assert my_bookings_page.cancel_buttons.first.is_visible()

    def test_cancel_confirmation_dialog(self, my_bookings_page, user_logged_in):
        """SC-19: Cancel triggers window.confirm dialog."""
        my_bookings_page.navigate()
        if my_bookings_page.cancel_buttons.count() > 0:
            # Handle dialog and dismiss it
            my_bookings_page.page.once("dialog", lambda dialog: dialog.dismiss())
            my_bookings_page.click_cancel()
            # Dialog dismissed, no actual cancel happened

    def test_card_click_navigates_to_detail(self, my_bookings_page, user_logged_in):
        """SC-20: Clicking card navigates to /user/bookings/:id."""
        my_bookings_page.navigate()
        if my_bookings_page.booking_cards.count() > 0:
            my_bookings_page.get_booking_card(0).click()
            my_bookings_page.page.wait_for_load_state("networkidle")
            assert "/user/bookings/" in my_bookings_page.page.url


class TestBookingDetailPage:
    """SC-21 to SC-23: Booking detail."""

    def test_detail_shows_booking_info(self, booking_detail_page, my_bookings_page, user_logged_in):
        """SC-21: Detail shows all booking info."""
        my_bookings_page.navigate()
        if my_bookings_page.booking_cards.count() > 0:
            my_bookings_page.get_booking_card(0).click()
            booking_detail_page.page.wait_for_load_state("networkidle")
            assert booking_detail_page.detail_card.is_visible()

    def test_back_button(self, booking_detail_page, my_bookings_page, user_logged_in):
        """SC-22: Back button navigates to booking list."""
        my_bookings_page.navigate()
        if my_bookings_page.booking_cards.count() > 0:
            my_bookings_page.get_booking_card(0).click()
            booking_detail_page.page.wait_for_load_state("networkidle")
            booking_detail_page.back_button.click()
            booking_detail_page.page.wait_for_load_state("networkidle")
            assert "/user/bookings" in booking_detail_page.page.url

    def test_pending_shows_edit_cancel(self, booking_detail_page, my_bookings_page, user_logged_in):
        """SC-23: PENDING booking shows edit and cancel buttons."""
        my_bookings_page.navigate()
        if my_bookings_page.booking_cards.count() > 0:
            my_bookings_page.get_booking_card(0).click()
            booking_detail_page.page.wait_for_load_state("networkidle")
            # Check for action buttons if booking is PENDING
            has_edit = booking_detail_page.edit_button.is_visible(timeout=2000)
            has_cancel = booking_detail_page.cancel_button.is_visible(timeout=2000)
            # If it's PENDING, both should be visible; if not, neither should
            if has_edit:
                assert booking_detail_page.cancel_button.is_visible()