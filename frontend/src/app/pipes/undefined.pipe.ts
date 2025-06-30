import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'undefined'
})
export class UndefinedPipe implements PipeTransform {

  transform(value: unknown, ...args: unknown[]): unknown {
    if (value == undefined)
      return '';
    else
      return value;
  }

}
