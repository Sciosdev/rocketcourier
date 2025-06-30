import { NbMenuItem } from '@nebular/theme';

export const admin_menu: NbMenuItem[] = [
  {
    title: 'Navegación',
    group: true,
    data: {
      permission: 'menu',
      resource: ['guest']
    },
  },
  {
    title: 'Inicio',
    icon: 'home',
    link: '/intranet/inicio',
    home: true,
    data: {
      permission: 'menu',
      resource: ['guest']
    },
  },
  {
    title: 'Carga de layout',
    icon: 'upload-outline',
    link: '/intranet/carga-layout',
    data: {
      permission: 'menu',
      resource: ['customer']
    },
  },
  {
    title: 'Consulta de información',
    icon: 'file-text-outline',
    link: '/intranet/consulta-registros',
    data: {
      permission: 'menu',
      resource: ['messenger', 'customer', 'admin', 'courier']
    },
  },
  {
    title: 'Administración',
    icon: 'settings-2-outline',
    data: {
      permission: 'menu',
      resource: ['admin']
    },
    children: [
      {
        title: 'Usuarios',
        icon: 'people-outline',
        link: '/intranet/gestion-usuarios',
        data: {
          permission: 'menu',
          resource: ['admin']
        },
      },
      {
        title: 'Tiendas',
        icon: 'shopping-bag-outline',
        link: '/intranet/gestion-tiendas',
        data: {
          permission: 'menu',
          resource: ['admin']
        },
      }
      ,
    ]
  },
  {
    title: 'Configuración',
    icon: 'settings-outline',
    link: '/intranet/configuracion',
    data: {
      permission: 'menu',
      resource: ['customer']
    },
  },
];