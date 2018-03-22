#version 130

/* This comes interpolated from the vertex shader */
in vec2 texcoord;

/* The 6 textures to be rendered */
uniform sampler2D texFront;
uniform sampler2D texBack;
uniform sampler2D texLeft;
uniform sampler2D texRight;
uniform sampler2D texTop;
uniform sampler2D texBottom;

uniform int antialiasing;

uniform vec2 pixelOffset[16];

uniform vec4 backgroundColor;

uniform vec2 cursorPos;

uniform bool drawCursor;

out vec4 color;

void main(void) {
	//Anti-aliasing
	vec4 colorN[16];

	for (int loop = 0; loop < antialiasing; loop++) {
		vec2 coord = texcoord+pixelOffset[loop];

		if (coord.y >= -0.333333333 && coord.y < 0.333333333) {
			//Left
			if (coord.x < -0.5) {
				colorN[loop] = vec4(texture(texLeft, vec2(coord.x*2+2, coord.y*1.5+0.5)).rgb, 1);
			}
			//Front
			else if (coord.x < 0) {
				colorN[loop] = vec4(texture(texFront, vec2(coord.x*2+1, coord.y*1.5+0.5)).rgb, 1);
			}
			//Right
			else if (coord.x < 0.5) {
				colorN[loop] = vec4(texture(texRight, vec2(coord.x*2, coord.y*1.5+0.5)).rgb, 1);
			}
			//Back
			else {
				colorN[loop] = vec4(texture(texBack, vec2(coord.x*2-1, coord.y*1.5+0.5)).rgb, 1);
			}
		}
		else if (coord.x < 0 && coord.x >= -0.5) {
			//Bottom
			if (coord.y < -0.333333333) {
				colorN[loop] = vec4(texture(texBottom, vec2(coord.x*2+1, coord.y*1.5+1.5)).rgb, 1);
			}
			//Top
			else {
				colorN[loop] = vec4(texture(texTop, vec2(coord.x*2+1, coord.y*1.5-0.5)).rgb, 1);
			}
		}
		else {
			colorN[loop] = backgroundColor;
		}

		if (drawCursor) {
			if (coord.x*2 + 0.006 >= cursorPos.x-1 && coord.x*2 - 0.006 < cursorPos.x-1 &&
				coord.y*3 + 0.012 >= cursorPos.y*2-1 && coord.y*3 - 0.012 < cursorPos.y*2-1) {
					colorN[loop] = vec4(1, 1, 1, 1);
			}
		}
	}

	if (antialiasing == 16) {
	  vec4 corner[4];
	  corner[0] = mix(mix(colorN[0], colorN[1], 2.0/3.0), mix(colorN[4], colorN[5], 3.0/5.0), 5.0/8.0);
	  corner[1] = mix(mix(colorN[3], colorN[2], 2.0/3.0), mix(colorN[7], colorN[6], 3.0/5.0), 5.0/8.0);
	  corner[2] = mix(mix(colorN[12], colorN[13], 2.0/3.0), mix(colorN[8], colorN[9], 3.0/5.0), 5.0/8.0);
	  corner[3] = mix(mix(colorN[15], colorN[14], 2.0/3.0), mix(colorN[11], colorN[10], 3.0/5.0), 5.0/8.0);
	  color = mix(mix(corner[0], corner[1], 0.5), mix(corner[2], corner[3], 0.5), 0.5);
	}
	else if (antialiasing == 4) {
		color = mix(mix(colorN[0], colorN[1], 0.5), mix(colorN[2], colorN[3], 0.5), 0.5);
	}
	else { //if antialiasing == 1
		color = colorN[0];
	}
}
