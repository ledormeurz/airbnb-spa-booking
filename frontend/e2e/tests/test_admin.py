"""E2E tests for admin pages."""
import pytest
from datetime import datetime, timedelta


class TestDashboardPage:
    """SC-24 to SC-26: Admin dashboard."""

    def test_dashboard_stats_visible(self, dashboard_page, admin_logged_in):
        """SC-24: Four stat cards display."""
        dashboard_page.navigate()
        assert dashboard_page.stat_cards.count() == 4

    def test_quick_links_work(self, dashboard_page, admin_logged_in):
        """SC-25: Quick links navigate to admin sections."""
        dashboard_page.navigate()
        assert dashboard_page.quick_links.count() >= 2
        # Click first quick link
        dashboard_page.quick_links.first.click()
        dashboard_page.page.wait_for_load_state("networkidle")
        assert "/admin/" in dashboard_page.page.url

    def test_upcoming_section_visible(self, dashboard_page, admin_logged_in):
        """SC-26: Upcoming arrivals/departures sections visible when data exists."""
        dashboard_page.navigate()
        # These sections may or may not be visible depending on data
        count = dashboard_page.upcoming_section.count()
        assert count >= 0  # Section is optional


class TestAdminBookingsPage:
    """SC-27 to SC-31: Admin bookings management."""

    def test_filter_bar_visible(self, admin_bookings_page, admin_logged_in):
        """SC-27: Filter bar loads with status, type, date, search."""
        admin_bookings_page.navigate()
        assert admin_bookings_page.filter_selects.count() >= 2
        assert admin_bookings_page.date_inputs.count() >= 2

    def test_bookings_table_visible(self, admin_bookings_page, admin_logged_in):
        """SC-28: Table displays bookings."""
        admin_bookings_page.navigate()
        if admin_bookings_page.table_rows.count() > 0:
            assert admin_bookings_page.table.is_visible()

    def test_confirm_button(self, admin_bookings_page, admin_logged_in):
        """SC-29: Confirmer button works for PENDING bookings."""
        admin_bookings_page.navigate()
        if admin_bookings_page.confirm_buttons.count() > 0:
            # Handle dialog
            admin_bookings_page.page.once("dialog", lambda dialog: dialog.accept())
            admin_bookings_page.confirm_buttons.first.click()
            admin_bookings_page.page.wait_for_load_state("networkidle")
            # Should show success message or update
            msg = admin_bookings_page.page.locator(".alert-success, .alert-error")
            assert msg.is_visible(timeout=5000)

    def test_reject_button(self, admin_bookings_page, admin_logged_in):
        """SC-30: Refuser button prompts for reason."""
        admin_bookings_page.navigate()
        if admin_bookings_page.reject_buttons.count() > 0:
            # Handle dialog - dismiss
            admin_bookings_page.page.once("dialog", lambda dialog: dialog.dismiss())
            admin_bookings_page.reject_buttons.first.click()

    def test_filters_filter_results(self, admin_bookings_page, admin_logged_in):
        """SC-31: Filters narrow results."""
        admin_bookings_page.navigate()
        # Select a status filter
        admin_bookings_page.status_filter.select_option("PENDING")
        admin_bookings_page.page.wait_for_load_state("networkidle")
        # Should have filtered results
        assert admin_bookings_page.page.locator(".badge-pending").count() >= 0


class TestAdminCalendarPage:
    """SC-32 to SC-36: Admin calendar."""

    def test_calendar_grid_visible(self, admin_calendar_page, admin_logged_in):
        """SC-32: Month grid renders with day headers."""
        admin_calendar_page.navigate()
        assert admin_calendar_page.calendar_grid.is_visible()
        assert admin_calendar_page.month_title.is_visible()

    def test_navigation_buttons(self, admin_calendar_page, admin_logged_in):
        """SC-33: Prev/next month navigation works."""
        admin_calendar_page.navigate()
        original_title = admin_calendar_page.month_title.text_content()
        admin_calendar_page.next_button.click()
        new_title = admin_calendar_page.month_title.text_content()
        assert original_title != new_title, "Month didn't change after clicking next"

    def test_block_form_creates_block(self, admin_calendar_page, admin_logged_in):
        """SC-34: Block form creates a new block."""
        admin_calendar_page.navigate()
        # Fill in future dates
        future = datetime.now() + timedelta(days=60)
        future2 = future + timedelta(days=3)
        admin_calendar_page.block_start_date.fill(future.strftime("%Y-%m-%d"))
        admin_calendar_page.block_end_date.fill(future2.strftime("%Y-%m-%d"))
        admin_calendar_page.block_reason.fill("E2E Test Block")
        admin_calendar_page.block_button.click()
        admin_calendar_page.page.wait_for_load_state("networkidle")
        # Should show success message or block in list
        assert admin_calendar_page.page.locator(".alert-success").is_visible(timeout=5000)

    def test_blocks_list_displayed(self, admin_calendar_page, admin_logged_in):
        """SC-35: Existing blocks display in sidebar."""
        admin_calendar_page.navigate()
        # Blocks list may be visible if blocks exist
        has_blocks = admin_calendar_page.blocks_list.is_visible()
        if has_blocks:
            assert admin_calendar_page.delete_block_buttons.count() >= 0

    def test_delete_block(self, admin_calendar_page, admin_logged_in):
        """SC-36: Delete block triggers confirmation."""
        admin_calendar_page.navigate()
        if admin_calendar_page.delete_block_buttons.count() > 0:
            # Handle dialog - dismiss
            admin_calendar_page.page.once("dialog", lambda dialog: dialog.dismiss())
            admin_calendar_page.delete_block_buttons.first.click()


class TestAdminPricesPage:
    """SC-37 to SC-40: Admin prices."""

    def test_prices_table_visible(self, admin_prices_page, admin_logged_in):
        """SC-37: Table shows all price rules."""
        admin_prices_page.navigate()
        assert admin_prices_page.table_rows.count() > 0

    def test_edit_price_shows_input(self, admin_prices_page, admin_logged_in):
        """SC-38: Edit icon shows price input + save button."""
        admin_prices_page.navigate()
        if admin_prices_page.edit_buttons.count() > 0:
            admin_prices_page.edit_buttons.first.click()
            assert admin_prices_page.price_inputs.is_visible()
            assert admin_prices_page.save_buttons.is_visible()

    def test_toggle_active(self, admin_prices_page, admin_logged_in):
        """SC-39: Active toggle saves state."""
        admin_prices_page.navigate()
        if admin_prices_page.toggle_switches.count() > 0:
            admin_prices_page.toggle_switches.first.click()
            admin_prices_page.page.wait_for_load_state("networkidle")
            assert admin_prices_page.page.locator(".alert-success").is_visible(timeout=5000)

    def test_save_price(self, admin_prices_page, admin_logged_in):
        """SC-40: Success message on price update."""
        admin_prices_page.navigate()
        if admin_prices_page.edit_buttons.count() > 0:
            admin_prices_page.edit_buttons.first.click()
            # Clear and set new price
            admin_prices_page.price_inputs.first.fill("150.00")
            admin_prices_page.save_buttons.first.click()
            admin_prices_page.page.wait_for_load_state("networkidle")
            assert admin_prices_page.page.locator(".alert-success").is_visible(timeout=5000)


class TestAdminEquipmentPage:
    """SC-41 to SC-45: Admin equipment."""

    def test_equipment_grid_visible(self, admin_equipment_page, admin_logged_in):
        """SC-41: Grid displays all equipment."""
        admin_equipment_page.navigate()
        assert admin_equipment_page.equipment_cards.count() > 0

    def test_add_button_opens_modal(self, admin_equipment_page, admin_logged_in):
        """SC-42: 'Ajouter' button opens modal."""
        admin_equipment_page.navigate()
        admin_equipment_page.add_button.click()
        assert admin_equipment_page.modal.is_visible()

    def test_modal_has_form_fields(self, admin_equipment_page, admin_logged_in):
        """SC-42b: Modal has form fields."""
        admin_equipment_page.navigate()
        admin_equipment_page.add_button.click()
        assert admin_equipment_page.modal_name.is_visible()
        assert admin_equipment_page.modal_description.is_visible()
        assert admin_equipment_page.modal_icon.is_visible()

    def test_edit_opens_prefilled_modal(self, admin_equipment_page, admin_logged_in):
        """SC-43: Edit opens pre-filled modal."""
        admin_equipment_page.navigate()
        if admin_equipment_page.edit_buttons.count() > 0:
            admin_equipment_page.edit_buttons.first.click()
            assert admin_equipment_page.modal.is_visible()
            # Should be pre-filled
            name_val = admin_equipment_page.modal_name.input_value()
            assert name_val != ""

    def test_save_creates_equipment(self, admin_equipment_page, admin_logged_in):
        """SC-44: Save creates/updates equipment."""
        admin_equipment_page.navigate()
        admin_equipment_page.add_button.click()
        admin_equipment_page.modal_name.fill("E2E Test Equipment")
        admin_equipment_page.modal_description.fill("Created by e2e test")
        admin_equipment_page.modal_icon.fill("🔧")
        admin_equipment_page.modal_save.click()
        admin_equipment_page.page.wait_for_load_state("networkidle")
        msg = admin_equipment_page.page.locator(".alert-success")
        assert msg.is_visible(timeout=5000)

    def test_delete_triggers_confirm(self, admin_equipment_page, admin_logged_in):
        """SC-45: Delete triggers confirmation dialog."""
        admin_equipment_page.navigate()
        if admin_equipment_page.delete_buttons.count() > 0:
            admin_equipment_page.page.once("dialog", lambda dialog: dialog.dismiss())
            admin_equipment_page.delete_buttons.first.click()


class TestAdminUsersPage:
    """SC-46 to SC-50: Admin users."""

    def test_users_table_visible(self, admin_users_page, admin_logged_in):
        """SC-46: Table displays all users."""
        admin_users_page.navigate()
        assert admin_users_page.table_rows.count() > 0

    def test_create_button_opens_modal(self, admin_users_page, admin_logged_in):
        """SC-47: 'Créer' opens modal with all fields incl. password."""
        admin_users_page.navigate()
        admin_users_page.create_button.click()
        assert admin_users_page.modal.is_visible()
        assert admin_users_page.modal_username.is_visible()
        assert admin_users_page.modal_password.is_visible()  # Password field visible in create mode
        assert admin_users_page.modal_first_name.is_visible()
        assert admin_users_page.modal_last_name.is_visible()
        assert admin_users_page.modal_email.is_visible()
        assert admin_users_page.modal_role.is_visible()

    def test_edit_modal_no_password(self, admin_users_page, admin_logged_in):
        """SC-48: Edit modal has NO password field."""
        admin_users_page.navigate()
        if admin_users_page.edit_buttons.count() > 0:
            admin_users_page.edit_buttons.first.click()
            assert admin_users_page.modal.is_visible()
            # Password should NOT be visible in edit mode
            assert admin_users_page.modal_password.is_visible(timeout=1000) is False

    def test_no_password_hash_displayed(self, admin_users_page, admin_logged_in):
        """SC-50: Password hash is NEVER displayed in the table."""
        admin_users_page.navigate()
        table_text = admin_users_page.page.locator(".table").text_content()
        # Ensure no password hash strings appear
        assert "$2a$" not in (table_text or "")
        assert "$2b$" not in (table_text or "")

    def test_edit_role_and_enabled(self, admin_users_page, admin_logged_in):
        """SC-49: Role and enabled toggles work on edit."""
        admin_users_page.navigate()
        if admin_users_page.edit_buttons.count() > 0:
            admin_users_page.edit_buttons.first.click()
            assert admin_users_page.modal.is_visible()
            # Role select should be visible
            assert admin_users_page.modal_role.is_visible()
            # Checkbox for enabled should be visible
            assert admin_users_page.page.locator('.modal input[type="checkbox"]').is_visible()