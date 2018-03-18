#version 130

#define M_PI 3.14159265
#define M_E 2.718281828

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

uniform float fovx;
uniform float fovy;

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

vec3 passthrough(vec2 coord) {
	return vec3(coord, -1);
}

//copied from github.com/shaunlebron/flex-fov
vec3 latlon_to_ray(float lat, float lon) {
  return vec3(
    sin(lon)*cos(lat),
    sin(lat),
    -cos(lon)*cos(lat)
  );
}

vec3 mercator_inverse(vec2 lenscoord) {
  float lon = lenscoord.x;
  float lat = atan(sinh(lenscoord.y*fovy/fovx));
  return latlon_to_ray(lat, lon);
}
vec2 mercator_forward(float lat, float lon) {
  float x = lon;
  float y = log(tan(M_PI*0.25+lat*0.5));
  return vec2(x,y);
}
vec3 mercator_ray(vec2 lenscoord) {
  float scale = mercator_forward(0, radians(fovx)/2).x;
  return mercator_inverse((lenscoord) * scale);
}

vec3 panini_inverse(vec2 lenscoord, float dist) {
  float x = lenscoord.x;
  float y = lenscoord.y*fovy/fovx;
  float d = dist;
  float k = x*x/((d+1)*(d+1));
  float dscr = k*k*d*d - (k+1)*(k*d*d-1);
  float clon = (-k*d+sqrt(dscr))/(k+1);
  float S = (d+1)/(d+clon);
  float lon = atan(x,S*clon);
  float lat = atan(y,S);
  return latlon_to_ray(lat, lon);
}
vec2 panini_forward(float lat, float lon, float dist) {
  float d = dist;
  float S = (d+1)/(d+cos(lon));
  float x = S*sin(lon);
  float y = S*tan(lat);
  return vec2(x,y);
}
vec3 panini_ray(vec2 lenscoord, float dist) {
  float scale = panini_forward(0, radians(fovx)/2, dist).x;
  return panini_inverse((lenscoord) * scale, dist);
}
//end copy

void main(void) {
	/* Ray-trace a cube */

	//Anti-aliasing
	vec4 colorN[16];

	for (int loop = 0; loop < antialiasing; loop++) {

		vec2 coord = texcoord + pixelOffset[loop];

		//create ray
		vec3 ray;

		if (fovx < 90) {
			ray = passthrough(vec2(coord.x, coord.y*fovy/fovx));
		} else if (fovx <= 180) {
			ray = panini_ray(coord, (fovx-90)/90);
		} else if (fovx < 320) {
			float linear = (fovx - 180)/ 140;
			float expon = linear*pow(M_E, 1-linear);
			ray = mix(panini_ray(coord, 1), mercator_ray(coord), expon);
		} else {
			ray = mercator_ray(coord);
		}

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
