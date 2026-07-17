import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface SpaSection {
  icon: string;
  title: string;
  description: string;
  features: string[];
}

interface SafetyRule {
  icon: string;
  text: string;
}

@Component({
  selector: 'app-spa',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './spa.component.html',
  styleUrl: './spa.component.css'
})
export class SpaComponent {
  sections: SpaSection[] = [
    {
      icon: '🫧',
      title: 'Jacuzzi Extérieur',
      description: 'Notre jacuzzi 4 places vous accueille dans un cadre intimiste avec vue sur la nature. L\'eau est chauffée à 37°C en continu et traitée avec des produits respectueux de l\'environnement.',
      features: ['Capacité : 4 personnes', 'Température : 37°C', 'Hydro-massage 20 jets', 'Éclairage LED chromothérapie', 'Couverture isolante']
    },
    {
      icon: '🧖',
      title: 'Sauna Finlandais',
      description: 'Un sauna en bois de cèdre rouge, chauffage au poêle électrique avec pierres volcaniques. L\'expérience nordique authentique pour une détox en profondeur.',
      features: ['Capacité : 3 personnes', 'Température : 70-90°C', 'Bois de cèdre rouge', 'Poêle à pierres volcaniques', 'Seau et louche inclus']
    },
    {
      icon: '🛋️',
      title: 'Espace Détente',
      description: 'Un espace cosy avec transats, coussins et plaids pour vous détendre entre deux sessions. Thé, infusion et eau fraîche à disposition.',
      features: ['Transats confortables', 'Plaids et coussins', 'Thé & infusions offerts', 'Musique d\'ambiance', 'Lumières tamisées']
    },
    {
      icon: '🚿',
      title: 'Douche Extérieure',
      description: 'Une douche à ciel ouvert alimentée en eau chaude, parfaite pour vous rafraîchir après le sauna. Entourée de bambous pour préserver votre intimité.',
      features: ['Eau chaude', 'Sol en teck', 'Intimité préservée', 'Serviettes fournies', 'Savon naturel']
    }
  ];

  safetyRules: SafetyRule[] = [
    { icon: '⏱️', text: 'Durée maximale conseillée : 15 minutes en jacuzzi, 10 minutes en sauna.' },
    { icon: '💧', text: 'Hydratez-vous abondamment avant, pendant et après chaque session.' },
    { icon: '🚫', text: 'Déconseillé aux femmes enceintes et aux personnes souffrant de problèmes cardiaques.' },
    { icon: '🧴', text: 'Douche obligatoire avant d\'utiliser le jacuzzi ou le sauna.' },
    { icon: '👙', text: 'Maillot de bain obligatoire dans le jacuzzi.' },
    { icon: '🔞', text: 'Surveillance parentale requise pour les mineurs.' }
  ];

  hoursOfUse = [
    { day: 'Lundi — Vendredi', hours: '16h — 21h' },
    { day: 'Samedi', hours: '10h — 22h' },
    { day: 'Dimanche', hours: '10h — 20h' }
  ];

  noiseGuidelines: string[] = [
    'Respectez le calme des lieux et des autres hôtes.',
    'Pas de musique amplifiée dans l\'espace spa.',
    'Éteignez votre téléphone ou mettez-le en silencieux.',
    'Parlez à voix basse dans les espaces communs.'
  ];
}