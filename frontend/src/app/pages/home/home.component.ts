import { Component, OnInit } from '@angular/core';
import { NbToastrService } from '@nebular/theme';
import { PrimeIcons, PrimeNGConfig } from 'primeng/api';
import { RegistroService } from '../../services/registro.service';
import { Estatus } from '../../models/estatus.model';
import { TipoEstatus } from 'src/app/models/tipo.estatus.model';
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  events: any[];
  registerKey: string = '';
  loading = false;
  estatusActual: Estatus;
  tipoEstatus: string = '';
  colorEstatus: string = '';
  mainEvents: any[];
  mainIcon: string;
  destino: any;
  orderKey;

  constructor(private route: ActivatedRoute,
    private primengConfig: PrimeNGConfig,
    protected registroService: RegistroService,
    private toastrService: NbToastrService,
    private router: Router
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }

 blueColor = '#3F5AA5';
 grayColor = '#DFDFDF';
  redColor = '#EA1A1A';
  yellowColor = '#E7C809';
  public id: string;


  ngOnInit(): void {

    

    this.events = [];
    this.mainIcon = "cogs";
    this.mainEvents = [
      {
        type: "Orden creada",
        color: this.grayColor,
        icon: "clock-o"
      },
      {
        type: "Preparando orden",
        color: this.grayColor,
        icon: "clock-o"
      },
      {
        type: "Entrega en curso",
        color: this.grayColor,
        icon: "clock-o"
      },
      {
        type: "Entregado",
        color: this.grayColor,
        icon: "clock-o"
      }
    ]

    this.route.queryParams
    .subscribe(params => {
      if(params.orderKey) {
        this.registerKey = params.orderKey;
        this.obtenerRegistro();
      }
    }
  );
   
  }

  public myMethodChangingQueryParams() {

    const queryParams: Params = { orderKey: this.registerKey };
  
    this.router.navigate(
      ['/'], 
      {
        relativeTo: this.route,
        queryParams: queryParams, 
        queryParamsHandling: 'merge', // remove to replace all query params by provided
      });
  }
  
  obtenerRegistro() {
    this.loading = true;
    this.events = [];
    this.registroService.obtenerEstatusLogRegistro(this.registerKey).subscribe(
      (response: any) => {
        this.loading = false;

        this.estatusActual = response.estatus_actual;
        this.orderKey = this.registerKey;

        switch (this.estatusActual.tipo) {
          case TipoEstatus.INICIAL:
            this.tipoEstatus = 'Orden creada con éxito';
            this.colorEstatus = 'callout callout-primary';
            this.mainIcon = "calendar-check-o";
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.grayColor,
                icon: "clock-o"
              },
              {
                type: "Entrega en curso",
                color: this.grayColor,
                icon: "clock-o"
              },
              {
                type: "Entregado",
                color: this.grayColor,
                icon: "clock-o"
              }
            ]
            break;
          case TipoEstatus.PROCESO:
            this.tipoEstatus = 'Preparando orden';
            this.colorEstatus = 'callout callout-info';
            this.mainIcon = "hourglass-half";
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.blueColor,
                icon: "hourglass-half"
              },
              {
                type: "Entrega en curso",
                color: this.grayColor,
                icon: "clock-o"
              },
              {
                type: "Entregado",
                color: this.grayColor,
                icon: "clock-o"
              }
            ]
            break;
          case TipoEstatus.TRANSITO:
            this.tipoEstatus = 'Entrega en curso';
            this.colorEstatus = 'callout callout-info';
            this.mainIcon = "rocket";
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.blueColor,
                icon: "hourglass-half"
              },
              {
                type: "Entrega en curso",
                color: this.blueColor,
                icon: "rocket"
              },
              {
                type: "Entregado",
                color: this.grayColor,
                icon: "clock-o"
              }
            ]
            break;
          case TipoEstatus.FINAL:
            this.tipoEstatus = 'Entregado';
            this.colorEstatus = 'callout callout-success';
            this.mainIcon = "check-square-o";
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.blueColor,
                icon: "hourglass-half"
              },
              {
                type: "Entrega en curso",
                color: this.blueColor,
                icon: "rocket"
              },
              {
                type: "Entregado",
                color: this.blueColor,
                icon: "check-square-o"
              }
            ]
            break;
          case TipoEstatus.EXCEPCION:
            this.tipoEstatus = 'Advertencia';
            this.colorEstatus = 'callout callout-warning';
            this.mainIcon = "exclamation-triangle";
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.blueColor,
                icon: "hourglass-half"
              },
              {
                type: "Excepción",
                color: this.redColor,
                icon: "exclamation-triangle"
              }
            ]
            break;
          case TipoEstatus.REASIGNACION:
            this.tipoEstatus = 'Preparando orden';
            this.colorEstatus = 'callout callout-info';
            this.mainIcon = "hourglass-half";
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.blueColor,
                icon: "hourglass-half"
              },
              {
                type: "Entrega en curso",
                color: this.grayColor,
                icon: "clock-o"
              },
              {
                type: "Entregado",
                color: this.grayColor,
                icon: "clock-o"
              }
            ]
            break;
        
          default:
            this.tipoEstatus = 'Preparando orden';
            this.colorEstatus = 'callout callout-warning';
            this.mainEvents = [
              {
                type: "Orden creada",
                color: this.blueColor,
                icon: "calendar-check-o"
              },
              {
                type: "Preparando orden",
                color: this.blueColor,
                icon: "hourglass-half"
              },
              {
                type: "Entrega en curso",
                color: this.grayColor,
                icon: "clock-o"
              },
              {
                type: "Entregado",
                color: this.grayColor,
                icon: "clock-o"
              }
            ]
            break;
        }

        this.events = response.historico;
        this.destino = response.destino;
        
      },
      (error) => {
        this.loading = false;
        this.toastrService.warning(error.error.responseMessage, 'Error al consultar');
      }
    );
  }

  public toDate(year: number, month:number, day: number): Date {
    let fecha = new Date();
    fecha.setUTCFullYear(year,month-1,day);
    return fecha;
  }
}
