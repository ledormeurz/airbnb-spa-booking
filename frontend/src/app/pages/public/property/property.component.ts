import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-property',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './property.component.html',
  styleUrl: './property.component.css'
})
export class PropertyComponent {
  property = {
    name: 'Studio Le Nid Spa',
    description: 'Notre studio de 35m² allie confort moderne et élégance naturelle. Entièrement rénové et décoré avec des matériaux nobles, il offre un espace cocooning pour deux personnes, avec un accès privatif à nos installations spa.',
    maxGuests: 2,
    bedrooms: 1,
    beds: 1,
    bathrooms: 1,
    amenities: [
      'WiFi haut débit gratuit',
      'TV écran plat avec Netflix',
      'Kitchenette équipée (plaques, frigo, micro-ondes, cafetière)',
      'Literie haut de gamme 180x200',
      'Salle de douche à l\'italienne',
      'Serviettes et peignoirs fournis',
      'Produits d\'accueil bio',
      'Entrée indépendante',
      'Terrasse privative',
      'Parking gratuit'
    ],
    rules: [
      'Arrivée à partir de 16h — Départ avant 11h',
      'Non-fumeur à l\'intérieur',
      'Ne convient pas aux enfants en bas âge',
      'Pas d\'événements / soirées',
      'Respect du calme après 22h',
      'Les animaux ne sont pas admis'
    ],
    checkInTime: '16:00',
    checkOutTime: '11:00'
  };
}