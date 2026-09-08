import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { PlatformLogoComponent } from "../platform-logo/platform-logo.component";
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';

@Component({
  selector: 'app-landing-navbar',
  imports: [
    RouterLink,
    MATERIAL_IMPORTS,
    PlatformLogoComponent
  ],
  templateUrl: './landing-navbar.component.html',
  styleUrl: './landing-navbar.component.scss'
})
export class LandingNavbarComponent {
  navigations = [
    { label: "Solutions", route: "/" },
    { label: "Features", route: "/" },
    { label: "Pricing", route: "/" },
    { label: "About", route: "/" }
  ]
}