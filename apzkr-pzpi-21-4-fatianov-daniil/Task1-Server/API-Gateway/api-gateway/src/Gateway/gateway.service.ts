import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createProxyMiddleware, Filter, RequestHandler } from 'http-proxy-middleware';
import { NextFunction, Request, Response } from 'express';

@Injectable()
export class GatewayService {
    constructor(private readonly configService: ConfigService) {}

    proxy(req: Request, res: Response, next: NextFunction, serviceUrlKey: string, path: string) {
        const targetUrl = this.configService.get<string>(serviceUrlKey);
        const proxy: RequestHandler = createProxyMiddleware({
            target: `${targetUrl}/${path}`,
            changeOrigin: true,
            pathRewrite: { [`^/gateway/${serviceUrlKey.toLowerCase()}/?`]: '' },
        });

        proxy(req, res, next);
    }
}