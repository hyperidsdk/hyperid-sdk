import { Buffer } from 'buffer';

window.global = window;
window.Buffer = Buffer;
window.process = {
    env: { DEBUG: 'foo' },
    argv: 'bar',
    version: '1.0.0'
};