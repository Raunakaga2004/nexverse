import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-platform-logo',
  imports: [RouterLink],
  templateUrl: './platform-logo.component.html',
  styleUrl: './platform-logo.component.scss'
})
export class PlatformLogoComponent {
  @Input() routerLink: string = "/";
  @Input() sizeInPx: number = 42;
  @Input() fontSizeInRem: number = 1.2;
}