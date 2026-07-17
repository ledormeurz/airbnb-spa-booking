import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface GalleryItem {
  id: number;
  label: string;
  icon: string;
  gradient: string;
}

interface GalleryCategory {
  id: string;
  label: string;
  items: GalleryItem[];
}

@Component({
  selector: 'app-gallery',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gallery.component.html',
  styleUrl: './gallery.component.css'
})
export class GalleryComponent {
  categories: GalleryCategory[] = [
    {
      id: 'studio',
      label: 'Studio',
      items: [
        { id: 1, label: 'Vue d\'ensemble', icon: '🛋️', gradient: 'linear-gradient(135deg, #2D2D2D, #4A3728)' },
        { id: 2, label: 'Coin salon', icon: '🛋️', gradient: 'linear-gradient(135deg, #4A3728, #6B5B4F)' },
        { id: 3, label: 'Chambre', icon: '🛏️', gradient: 'linear-gradient(135deg, #5C4A3A, #8B7355)' },
        { id: 4, label: 'Cuisine', icon: '🍳', gradient: 'linear-gradient(135deg, #6B5B4F, #9A8B7A)' },
        { id: 5, label: 'Salle de bain', icon: '🚿', gradient: 'linear-gradient(135deg, #3D3D3D, #6B6B6B)' },
        { id: 6, label: 'Terrasse', icon: '🌿', gradient: 'linear-gradient(135deg, #4A6741, #7A9B6E)' },
        { id: 7, label: 'Vue extérieure', icon: '🌅', gradient: 'linear-gradient(135deg, #8B7355, #C4A882)' },
        { id: 8, label: 'Entrée', icon: '🚪', gradient: 'linear-gradient(135deg, #2D2D2D, #5C4A3A)' }
      ]
    },
    {
      id: 'jacuzzi',
      label: 'Jacuzzi',
      items: [
        { id: 9, label: 'Jacuzzi vue jour', icon: '🫧', gradient: 'linear-gradient(135deg, #1A5276, #2980B9)' },
        { id: 10, label: 'Jacuzzi vue nuit', icon: '⭐', gradient: 'linear-gradient(135deg, #0D253F, #1A5276)' },
        { id: 11, label: 'Détails jets', icon: '💦', gradient: 'linear-gradient(135deg, #1A5276, #3498DB)' },
        { id: 12, label: 'Éclairage LED', icon: '🌈', gradient: 'linear-gradient(135deg, #6C3483, #8E44AD)' }
      ]
    },
    {
      id: 'sauna',
      label: 'Sauna',
      items: [
        { id: 13, label: 'Sauna intérieur', icon: '🧖', gradient: 'linear-gradient(135deg, #5D4037, #8D6E63)' },
        { id: 14, label: 'Poêle à pierres', icon: '🔥', gradient: 'linear-gradient(135deg, #BF360C, #E65100)' },
        { id: 15, label: 'Bancs en cèdre', icon: '🪵', gradient: 'linear-gradient(135deg, #4E342E, #795548)' },
        { id: 16, label: 'Ambiance', icon: '🕯️', gradient: 'linear-gradient(135deg, #3E2723, #5D4037)' }
      ]
    },
    {
      id: 'exterieur',
      label: 'Extérieur',
      items: [
        { id: 17, label: 'Vue sur la nature', icon: '🌳', gradient: 'linear-gradient(135deg, #2E7D32, #4CAF50)' },
        { id: 18, label: 'Chemin d\'accès', icon: '🌿', gradient: 'linear-gradient(135deg, #33691E, #689F38)' },
        { id: 19, label: 'Douche extérieure', icon: '🚿', gradient: 'linear-gradient(135deg, #004D40, #00897B)' },
        { id: 20, label: 'Parking', icon: '🅿️', gradient: 'linear-gradient(135deg, #37474F, #607D8B)' }
      ]
    },
    {
      id: 'equipements',
      label: 'Équipements',
      items: [
        { id: 21, label: 'TV connectée', icon: '📺', gradient: 'linear-gradient(135deg, #212121, #424242)' },
        { id: 22, label: 'Machine à café', icon: '☕', gradient: 'linear-gradient(135deg, #3E2723, #6D4C41)' },
        { id: 23, label: 'WiFi fibre', icon: '📶', gradient: 'linear-gradient(135deg, #1565C0, #42A5F5)' },
        { id: 24, label: 'Linge de lit', icon: '🧺', gradient: 'linear-gradient(135deg, #ECEFF1, #B0BEC5)' }
      ]
    }
  ];

  activeCategory = 'studio';
  lightboxOpen = false;
  lightboxItems: GalleryItem[] = [];
  lightboxIndex = 0;

  get currentCategoryItems(): GalleryItem[] {
    const cat = this.categories.find(c => c.id === this.activeCategory);
    return cat ? cat.items : [];
  }

  get lightboxItem(): GalleryItem {
    return this.lightboxItems[this.lightboxIndex];
  }

  setCategory(categoryId: string): void {
    this.activeCategory = categoryId;
  }

  openLightbox(item: GalleryItem): void {
    this.lightboxItems = this.currentCategoryItems;
    this.lightboxIndex = this.lightboxItems.findIndex(i => i.id === item.id);
    this.lightboxOpen = true;
  }

  closeLightbox(): void {
    this.lightboxOpen = false;
  }

  prevLightbox(): void {
    if (this.lightboxIndex > 0) {
      this.lightboxIndex--;
    }
  }

  nextLightbox(): void {
    if (this.lightboxIndex < this.lightboxItems.length - 1) {
      this.lightboxIndex++;
    }
  }
}