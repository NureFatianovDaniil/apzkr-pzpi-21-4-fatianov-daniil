import { All, Controller, Delete, Get, Next, Param, Post, Put, Req, Res } from '@nestjs/common';
import { GatewayService } from './gateway.service';
import { NextFunction, Request, Response } from 'express';

@Controller('/gateway/')
export class GatewayController {
    constructor(private readonly gatewayService: GatewayService) {}

    @All('user-service/:path*')
    userServiceProxy(@Req() req: Request, @Res() res: Response, @Next() next: NextFunction, @Param('path') path: string) {
        this.gatewayService.proxy(req, res, next, 'USER_SERVICE_URL', path);
    }

    @All('vehicle-station-service/:path*')
    vehicleStationServiceProxy(@Req() req: Request, @Res() res: Response, @Next() next: NextFunction, @Param('path') path: string) {
        this.gatewayService.proxy(req, res, next, 'VEHICLE_SERVICE_URL', path);
    }

    @All('order-service/:path*')
    orderServiceProxy(@Req() req: Request, @Res() res: Response, @Next() next: NextFunction, @Param('path') path: string) {
        this.gatewayService.proxy(req, res, next, 'ORDER_SERVICE_URL', path);
    }
}