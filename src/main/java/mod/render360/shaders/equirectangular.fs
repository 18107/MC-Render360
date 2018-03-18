#version 130

#define M_PI 3.14159265

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

uniform vec2 cursorPos;

uniform bool drawCursor;

uniform bool drawCircle;

uniform vec2 rotation;

out vec4 color;

vec3 rotate(vec3 ray, vec2 angle) {

  //rotate y
  float y = -sin(angle.y)*ray.z;
  float z = cos(angle.y)*ray.z;
  ray.y = y;
  ray.z = z;

  //rotate x
  float x = -sin(angle.x)*ray.z;
  z = cos(angle.x)*ray.z;
  ray.x = x;
  ray.z = z;

  return ray;
}

vec3 rotate2(vec3 ray, vec2 angle) {
	//rotate x
	float x = cos(angle.x)*ray.x - sin(angle.x)*ray.z;
	float z = cos(angle.x)*ray.z + sin(angle.x)*ray.x;
	ray.x = x;
	ray.z = z;

	//rotate y
	float y = cos(angle.y)*ray.y - sin(angle.y)*ray.z;
	z = cos(angle.y)*ray.z + sin(angle.y)*ray.y;
	ray.y = y;
	ray.z = z;

	return ray;
}

void main(void) {
  /* Ray-trace a cube */

	//Anti-aliasing
	vec4 colorN[16];

	for (int loop = 0; loop < antialiasing; loop++) {

    vec2 coord = texcoord;

		//create ray
		vec3 ray = vec3(0, 0, -1);

		//rotate ray
		ray = rotate(ray, vec2((coord.x+pixelOffset[loop].x)*M_PI, (coord.y+pixelOffset[loop].y)*M_PI/2)); //x (-pi to pi), y (-pi/2 to pi/2\n
		ray = rotate2(ray, vec2(-rotation.x*M_PI/180, rotation.y*M_PI/180));

		//find which side to use
		if (abs(ray.x) > abs(ray.y)) {
			if (abs(ray.x) > abs(ray.z)) {
				if (ray.x > 0) {
					//right
					float x = ray.z / ray.x;
					float y = ray.y / ray.x;
					colorN[loop] = vec4(texture(texRight, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//left
					float x = -ray.z / -ray.x;
					float y = ray.y / -ray.x;
					colorN[loop] = vec4(texture(texLeft, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			} else {
				if (ray.z > 0) {
					//back
					float x = -ray.x / ray.z;
					float y = ray.y / ray.z;
					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//front
					float x = ray.x / -ray.z;
					float y = ray.y / -ray.z;
					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			}
		} else {
			if (abs(ray.y) > abs(ray.z)) {
				if (ray.y > 0) {
					//top
					float x = ray.x / ray.y;
					float y = ray.z / ray.y;
					colorN[loop] = vec4(texture(texTop, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//bottom
					float x = ray.x / -ray.y;
					float y = -ray.z / -ray.y;
					colorN[loop] = vec4(texture(texBottom, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			} else {
				if (ray.z > 0) {
					//back
					float x = -ray.x / ray.z;
					float y = ray.y / ray.z;
					colorN[loop] = vec4(texture(texBack, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				} else {
					//front
					float x = ray.x / -ray.z;
					float y = ray.y / -ray.z;
					colorN[loop] = vec4(texture(texFront, vec2((x+1)/2, (y+1)/2)).rgb, 1);
				}
			}
		}

		if (drawCursor) {
			vec2 normalAngle = cursorPos*2 - 1;
			float x = ray.x / -ray.z;
			float y = ray.y / -ray.z;
			if (x <= normalAngle.x + 0.01 && y <= normalAngle.y + 0.01 &&
				x >= normalAngle.x - 0.01 && y >= normalAngle.y - 0.01 &&
				ray.z < 0) {
				colorN[loop] = vec4(1, 1, 1, 1);
			}
		} else if (drawCircle) {
			float phi = (coord.y+pixelOffset[loop].y)*M_PI/2;
			float lambda = (coord.x+pixelOffset[loop].x-rotation.x/180)*M_PI;
			float z = cos(phi)*cos(lambda);
			float y = sin(phi)*cos(rotation.y*M_PI/180+M_PI/2) + z*sin(rotation.y*M_PI/180+M_PI/2);
			float radius = asin(1-y);
			if (radius < 0.0013 && radius > 0.0007) {
				colorN[loop] = vec4(0, 0, 0, 1);
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
