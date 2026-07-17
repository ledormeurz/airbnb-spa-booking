import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  features = [
    { icon: '♨️', title: 'Jacuzzi Privatif', desc: 'Détendez-vous dans notre jacuzzi extérieur chauffé toute l\'année, accessible en toute intimité.' },
    { icon: '🧖', title: 'Sauna', desc: 'Profitez de notre sauna traditionnel pour une detox en profondeur et une relaxation absolue.' },
    { icon: '🌿', title: 'Nature & Calme', desc: 'Situé en pleine nature, le calme absolu vous permet de vous ressourcer loin du tumulte.' },
    { icon: '🏡', title: 'Studio Équipé', desc: 'Un studio tout confort avec kitchenette, wifi haut débit et entrée indépendante.' }
  ];
}