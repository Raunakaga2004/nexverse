import { Component } from '@angular/core';
import { LandingNavbarComponent } from "../../shared/components/landing-navbar/landing-navbar.component";
import { MATERIAL_IMPORTS } from '../../core/material/matrerial';

@Component({
  selector: 'app-landing',
  imports: [LandingNavbarComponent, MATERIAL_IMPORTS],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent {
}