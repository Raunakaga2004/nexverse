import { Component, Input } from '@angular/core';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';

@Component({
  selector: 'app-no-data',
  imports: [MATERIAL_IMPORTS,],
  templateUrl: './no-data.component.html',
  styleUrl: './no-data.component.scss'
})
export class NoDataComponent {
  @Input() icon = 'inbox';
  @Input() title = 'No data found';
  @Input() message = 'There is no data to display.';
}