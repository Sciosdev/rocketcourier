import { FullAddress } from './address.model';
export class UsuarioCompleto {
    address?: string;
    birthday?: string;
    commune?: string;
    documentCountry?: string;
    documentNumber?: string;
    documentType?: string;
    dv?: string;
    email?: string;
    firstName?: string;
    id: string;
    lastLogin?: string;
    lastName?: string;
    name?: string;
    patent?: string;
    phoneNumber?: string;
    rol: string;
    secondLastName?: string;
    user: string;
    vehicleData?: string;
    foto?: string;
    tienda?: number;
    password?: string;
    fullAddress?: FullAddress;

    constructor(){
        this.fullAddress = {
        };
    }

    setUsuario(usuario: UsuarioCompleto) {
        if(usuario.address) this.address = usuario.address;
        if(usuario.fullAddress) this.fullAddress = usuario.fullAddress;
        if(usuario.birthday) this.birthday = usuario.birthday;
        if(usuario.commune) this.commune = usuario.commune;
        if(usuario.documentCountry) this.documentCountry = usuario.documentCountry;
        if(usuario.documentNumber) this.documentNumber = usuario.documentNumber;
        if(usuario.documentType) this.documentType = usuario.documentType;
        if(usuario.dv) this.dv = usuario.dv;
        if(usuario.email) this.email = usuario.email;
        if(usuario.firstName) this.firstName = usuario.firstName;
        if(usuario.id) this.id = usuario.id;
        if(usuario.lastLogin) this.lastLogin = usuario.lastLogin;
        if(usuario.lastName) this.lastName = usuario.lastName;
        if(usuario.name) this.name = usuario.name;
        if(usuario.patent) this.patent = usuario.patent;
        if(usuario.phoneNumber) this.phoneNumber = usuario.phoneNumber;
        if(usuario.rol) this.rol = usuario.rol;
        if(usuario.secondLastName) this.secondLastName = usuario.secondLastName;
        if(usuario.user) this.user = usuario.user;
        if(usuario.vehicleData) this.vehicleData = usuario.vehicleData;
        if(usuario.foto) this.foto = usuario.foto;
        if(usuario.tienda) this.tienda = usuario.tienda;
        if(usuario.password) this.password = usuario.password;
    }
}