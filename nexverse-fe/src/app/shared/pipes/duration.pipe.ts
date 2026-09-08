import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'duration'
})
export class DurationPipe implements PipeTransform {

  transform(seconds: number | null | undefined): string {
    if (seconds == null || isNaN(seconds) || seconds < 0) {
      return '0 sec';
    }

    if (seconds < 60) {
      return `${Math.round(seconds)} sec`;
    }

    if (seconds < 3600) {
      return `${Math.floor(seconds / 60)} min`;
    }

    const hours = seconds / 3600;
    return `${hours.toFixed(1)} hr`;
  }
}