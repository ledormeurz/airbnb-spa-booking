import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { Equipment } from '../../../models/equipment.model';

@Component({
  selector: 'app-admin-equipment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-equipment.component.html',
  styleUrl: './admin-equipment.component.css'
})
export class AdminEquipmentComponent implements OnInit {
  private apiService = inject(ApiService);

  equipmentList: Equipment[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  showModal = false;
  editingId: number | null = null;
  form = {
    name: '',
    description: '',
    icon: '',
    active: true
  };

  ngOnInit(): void {
    this.loadEquipment();
  }

  loadEquipment(): void {
    this.loading = true;
    this.errorMessage = '';
    this.apiService.getAllEquipment().subscribe({
      next: (data) => {
        this.equipmentList = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Erreur lors du chargement des équipements.';
      }
    });
  }

  openAddModal(): void {
    this.editingId = null;
    this.form = { name: '', description: '', icon: '', active: true };
    this.showModal = true;
  }

  openEditModal(equipment: Equipment): void {
    this.editingId = equipment.id;
    this.form = {
      name: equipment.name,
      description: equipment.description,
      icon: equipment.icon,
      active: equipment.active
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.editingId = null;
  }

  saveEquipment(): void {
    if (!this.form.name || !this.form.description) {
      this.errorMessage = 'Veuillez remplir tous les champs obligatoires.';
      return;
    }

    if (this.editingId) {
      this.apiService.updateEquipment(this.editingId, this.form).subscribe({
        next: () => {
          this.successMessage = 'Équipement mis à jour avec succès.';
          this.closeModal();
          this.loadEquipment();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour.';
        }
      });
    } else {
      this.apiService.createEquipment(this.form).subscribe({
        next: () => {
          this.successMessage = 'Équipement ajouté avec succès.';
          this.closeModal();
          this.loadEquipment();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de la création.';
        }
      });
    }
  }

  deleteEquipment(id: number): void {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet équipement ?')) {
      this.apiService.deleteEquipment(id).subscribe({
        next: () => {
          this.successMessage = 'Équipement supprimé avec succès.';
          this.loadEquipment();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Erreur lors de la suppression.';
        }
      });
    }
  }
}